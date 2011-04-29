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
package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalesenses.binaryimage.BinaryImage;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapProgramReply;

public class OtapHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(OtapHandler.class);

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
    private final short settingTimeoutMultiplierMillis;

    private Channel channel = null;

    private BinaryImage program = null;
    private Set<Integer> devicesSelectedToProgram;

    private Map<Integer, OtapDevice> devicesToProgram;

    private ScheduledFuture<?> sendOtapInitRequestsSchedule;
    private final Runnable sendOtapInitRequestsRunnable = new Runnable() {

        private OtapInitRequest createRequest() {
            OtapInitRequest req = new OtapInitRequest();
            req.chunk_count = program.getChunkCount();
            req.max_re_requests = settingMaxRerequests;
            req.timeout_multiplier_ms = settingTimeoutMultiplierMillis;
            return req;
        }

        private void send(OtapInitRequest req) {
            log.debug("Sending otap init request: {}", req);
            channel.write(req);
        }

        @Override
        public void run() {
            if (channel != null) {
                OtapInitRequest req = createRequest();

                int currentDeviceCount = 0;
                for (Integer id : devicesSelectedToProgram) {

                    req.participating_devices.value[currentDeviceCount] = id;
                    currentDeviceCount++;

                    if (currentDeviceCount == maxDevicesPerPacket) {
                        send(req);
                        currentDeviceCount = 0;
                        req = createRequest();
                    }
                }

                if (currentDeviceCount > 0 && currentDeviceCount < maxDevicesPerPacket)
                    send(req);
            }
        }
    };

    private ScheduledFuture<?> sendOtapProgramRequestsSchedule;
    private final Runnable sendOtapProgramRequestsRunnable = new Runnable() {
        @Override
        public void run() {
            if (channel != null) {
                // channel.write(new PresenceDetectRequest());
                log.trace("Sent Propram Request");
            }
        }
    };

    private ScheduledFuture<?> switchToProgrammingStateSchedule;
    private final Runnable switchToProgrammingStateRunnable = new Runnable() {

        @Override
        public void run() {
            if (state == State.OTAP_INIT) {
                log.info("Switching from state {} to {}.", state, State.OTAP_PROGRAM);
                state = State.OTAP_PROGRAM;
                sendOtapInitRequestsSchedule.cancel(false);

                sendOtapProgramRequestsSchedule =
                        executorService.scheduleWithFixedDelay(sendOtapProgramRequestsRunnable, 0, otapProgramInterval,
                                otapProgramIntervalTimeUnit);

            } else {
                log.error("Unable to switch from state {} to {}.", state, State.OTAP_PROGRAM);
            }

        }
    };

    public OtapHandler(final ScheduledExecutorService executorService, short settingMaxRerequests,
            short settingTimeoutMultiplierMillis) {
        this.executorService = executorService;
        this.settingMaxRerequests = settingMaxRerequests;
        this.settingTimeoutMultiplierMillis = settingTimeoutMultiplierMillis;

    }

    public void startProgramming(Set<Integer> devices, BinaryImage program) {
        stopProgramming();

        devicesSelectedToProgram = new HashSet<Integer>(devices);
        devicesToProgram = new HashMap<Integer, OtapDevice>();

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
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        stopProgramming();
        channel = null;
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        assert channel == null;
        channel = e.getChannel();
        super.channelConnected(ctx, e);
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof OtapInitReply) {
            handleOtapInitReply((OtapInitReply) message);
        } else if (message instanceof OtapProgramReply) {
            handleOtapProgramReply((OtapProgramReply) message);
        } else {
            super.messageReceived(ctx, e);
        }

    }

    private void handleOtapInitReply(OtapInitReply reply) {

        if (state != State.OTAP_INIT) {
            log.warn("Ignoring otap init reply in wrong state {}: {}", state, reply);
            return;
        }

        log.debug("Init Reply Received: {}", reply);

        if (!devicesToProgram.containsKey(reply.device_id)) {
            devicesToProgram.put(reply.device_id, new OtapDevice(reply.device_id));
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

    private void handleOtapProgramReply(OtapProgramReply message) {

        if (state != State.OTAP_PROGRAM) {
            log.warn("Ignoring otap program reply in wrong state {}: {}", state, message);
            return;
        }

        // TODO Auto-generated method stub

    }

}
