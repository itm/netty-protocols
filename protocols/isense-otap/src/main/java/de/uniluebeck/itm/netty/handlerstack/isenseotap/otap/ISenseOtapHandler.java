/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.netty.handlerstack.isenseotap.otap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalesenses.binaryimage.BinaryImage;
import com.coalesenses.binaryimage.OtapChunk;
import com.coalesenses.binaryimage.OtapPacket;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapDevice;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapProgramState;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.commands.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapProgramReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.tr.util.TimeDiff;

public class ISenseOtapHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {
    private final Logger log;

    private final int maxDevicesPerPacket = new OtapInitRequest().participating_devices.value.length;

    private enum State {
        IDLE, OTAP_INIT, OTAP_PROGRAM
    };

    private State state = State.IDLE;

    private final ScheduledExecutorService executorService;

    private final long otapInitInterval = 500;// TODO correct values
    private final TimeUnit otapInitIntervalTimeUnit = TimeUnit.MILLISECONDS;// TODO correct values

    private final long otapInitMaxDuration = 30; // TODO correct values
    private final TimeUnit otapInitMaxDurationTimeUnit = TimeUnit.SECONDS; // TODO correct values

    private final long otapProgramInterval = 500; // TODO correct values
    private final TimeUnit otapProgramIntervalTimeUnit = TimeUnit.MILLISECONDS; // TODO correct values

    private final short settingMaxRerequests;
    private final short settingTimeoutMultiplier;

    private TimeDiff chunkTimeout = new TimeDiff();

    private BinaryImage program = null;

    /** A set of devices selected to program, they must all ack before they are programmed */
    private Set<Integer> devicesSelectedToProgram;

    /** The actual set of devices that are reprogrammed */
    private Map<Integer, ISenseOtapDevice> devicesToProgram;

    private Set<Integer> failedDevices;

    private OtapChunk chunk = null;

    /** The remaining packets in the current chunk. These have not been received by all devices yet. */
    private Set<OtapPacket> remainingPacketsInChunk = new TreeSet<OtapPacket>();

    private ScheduledFuture<?> sendOtapInitRequestsSchedule;
    private final Runnable sendOtapInitRequestsRunnable = new Runnable() {

        private OtapInitRequest createRequest() {
            OtapInitRequest req = new OtapInitRequest();
            req.chunk_count = program.getChunkCount();
            req.max_re_requests = settingMaxRerequests;
            req.timeout_multiplier_ms = settingTimeoutMultiplier;
            return req;
        }

        private void send(OtapInitRequest req) {
            log.debug("Sending otap init request: {}", req);

            Channel channel = context.getChannel();

            DownstreamMessageEvent msg =
                    new DownstreamMessageEvent(channel, Channels.succeededFuture(channel), req,
                            channel.getRemoteAddress());
            context.sendDownstream(msg);
        }

        @Override
        public void run() {
            if (context == null)
                return;

            OtapInitRequest req = createRequest();

            // Send a number of otap init requests with maxDevicesPerPacket devices per request (size issue)
            int currentDeviceCount = 0;
            for (Integer id : devicesSelectedToProgram) {
                req.participating_devices.value[currentDeviceCount] = id;
                req.participating_devices.count++;
                currentDeviceCount++;

                if (currentDeviceCount == maxDevicesPerPacket) {
                    send(req);
                    currentDeviceCount = 0;
                    req = createRequest();
                }
            }

            // Send the last packet (if there is one)
            if (currentDeviceCount > 0 && currentDeviceCount < maxDevicesPerPacket)
                send(req);
        }
    };

    private ScheduledFuture<?> switchToProgrammingStateSchedule;
    private final Runnable switchToProgrammingStateRunnable = new Runnable() {

        @Override
        public void run() {
            if (state == State.OTAP_INIT) {
                // Switch state
                log.info("Switching from state {} to {}.", state, State.OTAP_PROGRAM);
                state = State.OTAP_PROGRAM;
                failedDevices = new HashSet<Integer>();

                // Cancel otap init schedule
                sendOtapInitRequestsSchedule.cancel(false);

                // Calculate chunk timeout
                int maxPacketsPerChunk = 0;
                for (int i = 0; i < program.getChunkCount(); ++i)
                    maxPacketsPerChunk = Math.max(maxPacketsPerChunk, program.getPacketCount(i, i));

                int magicTimeoutMillis = settingMaxRerequests * maxPacketsPerChunk * settingTimeoutMultiplier + 10000;
                chunkTimeout.setTimeOutMillis(magicTimeoutMillis);
                log.debug("Setting chunk timeout to {} ms.", magicTimeoutMillis);

                // Prepare the first chunk transmission
                prepareChunk(0);

                // Schedule the runnable to send program requests
                sendOtapProgramRequestsSchedule =
                        executorService.scheduleWithFixedDelay(sendOtapProgramRequestsRunnable, 0, otapProgramInterval,
                                otapProgramIntervalTimeUnit);

            } else {
                log.error("Unable to switch from state {} to {}.", state, State.OTAP_PROGRAM);
            }

        }
    };

    private ScheduledFuture<?> sendOtapProgramRequestsSchedule;
    private final Runnable sendOtapProgramRequestsRunnable = new Runnable() {
        @Override
        public void run() {
            if (context == null)
                return;

            if (state != State.OTAP_PROGRAM) {
                log.warn("Not in state {} but {}, returning.", State.OTAP_PROGRAM, state);
                return;
            }

            // Check if skip to next chunk or if we are done
            checkLeapToNextChunk();

            // Send the remaining packets
            if (remainingPacketsInChunk.size() > 0) {
                // Get the next remaining packet and remove it (until somebody re-requests it)
                OtapPacket packet = remainingPacketsInChunk.iterator().next();
                remainingPacketsInChunk.remove(packet);

                // Prepare the request using Fabric
                OtapProgramRequest req = new OtapProgramRequest();

                req.chunk_no = chunk.getChunkNumber();
                req.index = (short) packet.getIndex();
                req.packets_in_chunk = (byte) chunk.getPacketCount();
                req.overall_packet_no = packet.getOverallPacketNumber();
                req.remaining = (short) remainingPacketsInChunk.size();

                byte[] code = packet.getContent();
                req.code.count = (short) code.length;
                for (int i = 0; i < req.code.value.length; i++) {
                    req.code.value[i] = (byte) 0xFF;
                }
                System.arraycopy(code, 0, req.code.value, 0, req.code.count);

                log.debug("Sending packet with " + req.code.count + " bytes. Chunk[" + req.chunk_no + "], Index["
                        + req.index + "], PacketsInChunk[" + req.packets_in_chunk + "], OverallPacketNo["
                        + req.overall_packet_no + "], Remaining[" + req.remaining + "], RevisionNo[" + "]");

                // Send the packet
                HandlerTools.sendDownstream(req, context);
            }

        }
    };

    private ChannelHandlerContext context;

    public ISenseOtapHandler(final String instanceName, final ScheduledExecutorService executorService,
            short settingMaxRerequests, short settingTimeoutMultiplier) {

        log = LoggerFactory.getLogger((instanceName != null) ? instanceName : ISenseOtapHandler.class.getName());
        this.executorService = executorService;
        this.settingMaxRerequests = settingMaxRerequests;
        this.settingTimeoutMultiplier = settingTimeoutMultiplier;

    }

    public void startProgramming(Set<Integer> devices, BinaryImage program) {
        stopProgramming();

        this.program = program;

        devicesSelectedToProgram = new HashSet<Integer>(devices);
        devicesToProgram = new HashMap<Integer, ISenseOtapDevice>();

        sendOtapInitRequestsSchedule =
                executorService.scheduleWithFixedDelay(sendOtapInitRequestsRunnable, 0, otapInitInterval,
                        otapInitIntervalTimeUnit);

        switchToProgrammingStateSchedule =
                executorService.schedule(switchToProgrammingStateRunnable, otapInitMaxDuration,
                        otapInitMaxDurationTimeUnit);

        state = State.OTAP_INIT;
    }

    public void stopProgramming() {
        if (sendOtapInitRequestsSchedule != null)
            sendOtapInitRequestsSchedule.cancel(false);

        if (sendOtapProgramRequestsSchedule != null)
            sendOtapProgramRequestsSchedule.cancel(false);

        if (switchToProgrammingStateSchedule != null)
            switchToProgrammingStateSchedule.cancel(false);

        state = State.IDLE;
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (message instanceof ISenseOtapProgramRequest) {

            ISenseOtapProgramRequest request = (ISenseOtapProgramRequest) message;
            log.debug("Received programming request. Starting to program...");
            startProgramming(request.getOtapDevices(), new BinaryImage(request.getOtapProgram()));

        } else {
            super.writeRequested(ctx, e);
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof OtapInitReply) {
            OtapInitReply reply = (OtapInitReply) message;

            if (state == State.OTAP_INIT) {
                handleOtapInitReply(reply);
            } else {
                log.warn("Ignoring otap init reply in wrong state {}: {}", state, reply);
            }

        } else if (message instanceof OtapProgramReply) {
            OtapProgramReply reply = (OtapProgramReply) message;

            if (state == State.OTAP_PROGRAM) {

                ISenseOtapDevice device = devicesToProgram.get(reply.device_id);
                if (device != null) {
                    handleOtapProgramReply(device, reply);
                } else {
                    log.debug("Ignoring otap program reply from unknown device {}: {}", reply.device_id, reply);
                }

            } else {
                log.warn("Ignoring otap program reply in wrong state {}: {}", state, reply);
            }

        } else {
            super.messageReceived(ctx, e);
        }

    }

    private void handleOtapInitReply(OtapInitReply reply) {

        log.debug("Init Reply Received: {}", reply);

        if (!devicesToProgram.containsKey(reply.device_id)) {
            devicesToProgram.put(reply.device_id, new ISenseOtapDevice(reply.device_id));
            log.debug("Added new participating device {}, now got {} out of {} desired.", new Object[] {
                    reply.device_id, devicesToProgram.size(), devicesSelectedToProgram.size() });
        }

        // Check if all devices have sent acks
        if (devicesSelectedToProgram.size() == devicesToProgram.keySet().size()) {
            log.info("All {} devices have acknowledged. Switching to program state", devicesToProgram.size());
            switchToProgrammingStateSchedule.cancel(false);
            switchToProgrammingStateSchedule =
                    executorService.schedule(switchToProgrammingStateRunnable, 0, TimeUnit.MILLISECONDS);

        }

    }

    private void handleOtapProgramReply(ISenseOtapDevice device, OtapProgramReply reply) {
        // Only react to matching replies, discard old ones
        if (chunk.getChunkNumber() != reply.chunk_no) {
            log.trace("Ignoring old message from {} with chunk {}.", reply.device_id, reply.chunk_no);
            return;
        }

        // Ignore replies for devices that have completed the current chunk
        if (device.getChunkNo() == reply.chunk_no && device.isChunkComplete()) {
            log.trace("Ignoring message from {}, chunk {} is complete.", reply.device_id, reply.chunk_no);
            return;
        }

        // Update the device to the current chunk
        device.setChunkNo(reply.chunk_no);

        if (reply.missing_indices.count > 0) {
            Set<Integer> missingIndices = new HashSet<Integer>();

            // Add the missing packets to the set of remaining packets in this chunk
            for (int i = 0; i < reply.missing_indices.count; ++i) {
                int packetIndex = reply.missing_indices.value[i];
                OtapPacket packet = chunk.getPacketByIndex(packetIndex);

                if (packet != null) {
                    missingIndices.add(packetIndex);
                    remainingPacketsInChunk.add(packet);
                } else {
                    log.warn("Requested packet index {} not found in chunk {}", packetIndex, reply.chunk_no);
                }
            }

            if (log.isDebugEnabled())
                log.debug("Device {} misses packets {}", reply.device_id, Arrays.toString(missingIndices.toArray()));

            sendStateUpstream();
            
        } else {
            log.debug("No missing indices at {}, chunk ", Integer.toHexString(reply.device_id), reply.chunk_no);
            device.setChunkComplete(true);
        }

    }

    /**
    *
    */
    private synchronized void prepareChunk(int number) {
        chunk = program.getChunk(number);
        remainingPacketsInChunk.clear();
        chunkTimeout.touch();

        if (chunk != null) {
            log.info("Preparing chunk {} out of {}", number, program.getChunkCount());

            // Reset the chunk-complete flag on all devices
            for (ISenseOtapDevice d : devicesToProgram.values()) {
                d.setChunkComplete(false);
            }

            // Set the remaining packets to all packets in this chunk
            remainingPacketsInChunk.addAll(chunk.getPackets());
            log.debug("New chunk #{}, got " + remainingPacketsInChunk.size() + " packets to transmit", number);

        } else {
            log.info("No more chunks available. Stopping OTAP. Done.");
            sendStateUpstream();
            stopProgramming();
        }

    }

    /**
    *
    */
    private synchronized void checkLeapToNextChunk() {
        // Remove failed devices
        List<ISenseOtapDevice> failedDevicesToRemove = new LinkedList<ISenseOtapDevice>();

        for (ISenseOtapDevice device : devicesToProgram.values()) {
            if (device.getChunkNo() + 1 < chunk.getChunkNumber()) {
                failedDevicesToRemove.add(device);
                log.warn(
                        "Device {} failed (still in chunk {}, current is {}. Removing it.",
                        new Object[] { Integer.toHexString(device.getId()), device.getChunkNo(), chunk.getChunkNumber() });
            }
        }

        for (ISenseOtapDevice d : failedDevicesToRemove) {
            this.failedDevices.add(d.getId());
            devicesToProgram.remove(d.getId());
        }

        if (chunkTimeout.isTimeout() || getAllDevicesReceivedAllPacketsInChunk()) {
            log.info("OTAP::Leaping to next chunk. Still remaining packets [" + remainingPacketsInChunk.size() + "]");
            prepareChunk(chunk.getChunkNumber() + 1);
        }
    }

    /**
    *
    */
    private boolean getAllDevicesReceivedAllPacketsInChunk() {

        if (devicesToProgram.size() <= 0) {
            return false;
        }

        boolean allPacketsAtAllDevicesRX = true;
        for (ISenseOtapDevice d : devicesToProgram.values()) {
            if (!d.isChunkComplete()) {
                allPacketsAtAllDevicesRX = false;
            }
        }

        if (allPacketsAtAllDevicesRX)
            log.debug("All devices received all packets in chunk");

        return allPacketsAtAllDevicesRX;
    }

    private void sendStateUpstream() {
        HandlerTools.sendUpstream(getCurrentState(), context);
    }

    private ISenseOtapProgramState getCurrentState() {
        Set<Integer> devicesToBeProgrammed = new HashSet<Integer>(devicesToProgram.keySet());
        Set<Integer> failedDevices = new HashSet<Integer>(this.failedDevices);
        Set<Integer> doneDevices = new HashSet<Integer>();

        for (ISenseOtapDevice device : devicesToProgram.values())
            if (device.isChunkComplete() && device.getChunkNo() == program.getChunkCount() - 1)
                doneDevices.add(device.getId());

        return new ISenseOtapProgramState(devicesToBeProgrammed, failedDevices, doneDevices);

    }

    @Override
    public void afterAdd(ChannelHandlerContext ctx) throws Exception {
        assert context == null;
        this.context = ctx;
    }

    @Override
    public void afterRemove(ChannelHandlerContext ctx) throws Exception {
        this.context = null;
    }

    @Override
    public void beforeAdd(ChannelHandlerContext ctx) throws Exception {
        // Nothing to do
    }

    @Override
    public void beforeRemove(ChannelHandlerContext ctx) throws Exception {
        // Nothing to do
    }

}
