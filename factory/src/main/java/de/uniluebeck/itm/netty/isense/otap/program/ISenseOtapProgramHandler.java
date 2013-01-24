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
package de.uniluebeck.itm.netty.isense.otap.program;

import de.uniluebeck.itm.netty.isense.otap.ISenseOtapChunk;
import de.uniluebeck.itm.netty.isense.otap.ISenseOtapDevice;
import de.uniluebeck.itm.netty.isense.otap.ISenseOtapImage;
import de.uniluebeck.itm.netty.isense.otap.ISenseOtapPacket;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapProgramReply;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapProgramRequest;
import de.uniluebeck.itm.netty.util.HandlerTools;
import de.uniluebeck.itm.tr.util.TimeDiff;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

public class ISenseOtapProgramHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {

	private final Logger log;

	private final ScheduledExecutorService executorService;

	private final long otapProgramInterval = 27; // TODO correct values

	private final TimeUnit otapProgramIntervalTimeUnit = TimeUnit.MILLISECONDS; // TODO correct values

	private final short settingMaxReRequests;

	private final short settingTimeoutMultiplier;

	private ChannelHandlerContext context;

	private ISenseOtapProgramResult programStatus;

	private TimeDiff chunkTimeout = new TimeDiff();

	/**
	 * The actual set of devices that are reprogrammed
	 */
	private final Map<Integer, ISenseOtapDevice> devicesToProgram = new HashMap<Integer, ISenseOtapDevice>();

	private ISenseOtapChunk chunk = null;

	private ISenseOtapImage programImage = null;

	/**
	 * The remaining packets in the current chunk. These have not been received by all devices yet.
	 */
	private Set<ISenseOtapPacket> remainingPacketsInChunk = new TreeSet<ISenseOtapPacket>();

	private ScheduledFuture<?> sendOtapProgramRequestsSchedule;

	private final Runnable sendOtapProgramRequestsRunnable = new Runnable() {
		@Override
		public void run() {
			if (context == null) {
				return;
			}

			if (programImage == null) {
				log.warn("Not in programming state");
				return;
			}

			// Check if skip to next chunk or if we are done
			checkLeapToNextChunk();

			// Send the remaining packets
			if (remainingPacketsInChunk.size() > 0) {
				// Get the next remaining packet and remove it (until somebody re-requests it)
				ISenseOtapPacket packet = remainingPacketsInChunk.iterator().next();
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
						+ req.overall_packet_no + "], Remaining[" + req.remaining + "], RevisionNo[" + "]"
				);

				// Send the packet
				HandlerTools.sendDownstream(req, context);
			}

		}
	};

	public ISenseOtapProgramHandler(final String instanceName, final ScheduledExecutorService executorService,
									short settingMaxReRequests, short settingTimeoutMultiplier) {

		log = LoggerFactory.getLogger((instanceName != null) ? instanceName : ISenseOtapProgramHandler.class.getName());
		this.executorService = executorService;
		this.settingMaxReRequests = settingMaxReRequests;
		this.settingTimeoutMultiplier = settingTimeoutMultiplier;

	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object message = e.getMessage();

		if (message instanceof ISenseOtapProgramRequest) {
			startProgramming((ISenseOtapProgramRequest) message);
		} else {
			super.writeRequested(ctx, e);
		}

	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		Object message = e.getMessage();

		if (message instanceof OtapProgramReply) {
			OtapProgramReply reply = (OtapProgramReply) message;

			if (programImage != null) {

				ISenseOtapDevice device = devicesToProgram.get(reply.device_id);
				if (device != null) {
					handleOtapProgramReply(device, reply);
				} else {
					log.debug("Ignoring otap program reply from unknown device {}", reply.device_id);
				}

			} else {
				log.debug("Ignoring otap program reply in wrong state");
			}

		} else {
			super.messageReceived(ctx, e);
		}

	}

	public void startProgramming(ISenseOtapProgramRequest programRequest) {
		stopProgramming();

		log.info("Received programming request {}. Starting to program...", programRequest);

		this.programStatus = new ISenseOtapProgramResult(programRequest.getDevicesToProgram());

		for (Integer deviceId : programRequest.getDevicesToProgram()) {
			devicesToProgram.put(deviceId, new ISenseOtapDevice(deviceId));
		}

		programImage = new ISenseOtapImage(programRequest.getOtapProgram());

		// Calculate chunk timeout
		{
			int maxPacketsPerChunk = 0;
			for (int i = 0; i < programImage.getChunkCount(); ++i) {
				maxPacketsPerChunk = Math.max(maxPacketsPerChunk, programImage.getPacketCount(i, i));
			}

			int magicTimeoutMillis = settingMaxReRequests * maxPacketsPerChunk * settingTimeoutMultiplier + 10000;
			chunkTimeout.setTimeOutMillis(magicTimeoutMillis);
			log.debug("Setting chunk timeout to {} ms.", magicTimeoutMillis);

			// Prepare the first chunk transmission
			prepareChunk(0);
		}

		// Schedule the runnable to send program requests
		sendOtapProgramRequestsSchedule =
				executorService.scheduleWithFixedDelay(sendOtapProgramRequestsRunnable, 0, otapProgramInterval,
						otapProgramIntervalTimeUnit
				);
	}

	public void stopProgramming() {
		if (sendOtapProgramRequestsSchedule != null) {
			sendOtapProgramRequestsSchedule.cancel(false);
		}

		sendOtapProgramRequestsSchedule = null;
		programImage = null;
		devicesToProgram.clear();
	}

	private void handleOtapProgramReply(ISenseOtapDevice device, OtapProgramReply reply) {
		// Only react to matching replies, discard old ones
		if (chunk.getChunkNumber() != reply.chunk_no) {
			log.trace("Ignoring old message from {} with chunk {}.", reply.device_id, reply.chunk_no);
			return;
		}

		// Ignore replies for devices that have completed the current chunk
		if (device.getChunkNo() == reply.chunk_no && device.isChunkComplete()) {
			log.trace("Ignoring message from {}, chunk {} is complete on this device.", reply.device_id, reply.chunk_no
			);
			return;
		}

		// Update the device to the current chunk
		device.setChunkNo(reply.chunk_no);

		if (reply.missing_indices.count > 0) {
			Set<Integer> missingIndices = new HashSet<Integer>();

			// Add the missing packets to the set of remaining packets in this chunk
			for (int i = 0; i < reply.missing_indices.count; ++i) {
				int packetIndex = reply.missing_indices.value[i];
				ISenseOtapPacket packet = chunk.getPacketByIndex(packetIndex);

				if (packet != null) {
					missingIndices.add(packetIndex);
					remainingPacketsInChunk.add(packet);
				} else {
					log.warn("Requested packet index {} not found in chunk {}", packetIndex, reply.chunk_no);
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("Device {} misses packets {}", reply.device_id, Arrays.toString(missingIndices.toArray()));
			}

		} else {
			log.debug("No missing indices at {}, chunk {}", Integer.toHexString(reply.device_id), reply.chunk_no);
			device.setChunkComplete(true);

			if (isDeviceFullyProgrammed(device)) {
				programStatus.addDoneDevice(device.getId());
				log.info("Device {} is fully programmed. Now got {} done devices.", Integer.toHexString(device.getId()),
						programStatus
								.getDoneDevices().size()
				);
			}
		}

	}

	private boolean isDeviceFullyProgrammed(ISenseOtapDevice device) {
		return device.getChunkNo() + 1 == programImage.getChunkCount() && device.isChunkComplete();
	}

	/**
	 *
	 */
	private synchronized void prepareChunk(int number) {
		chunk = programImage.getChunk(number);
		remainingPacketsInChunk.clear();
		chunkTimeout.touch();

		if (chunk != null) {
			log.info("Preparing chunk {} out of {}", number, programImage.getChunkCount());

			// Reset the chunk-complete flag on all devices
			for (ISenseOtapDevice d : devicesToProgram.values()) {
				d.setChunkComplete(false);
			}

			// Set the remaining packets to all packets in this chunk
			remainingPacketsInChunk.addAll(chunk.getPackets());
			log.debug("New chunk #{}, got " + remainingPacketsInChunk.size() + " packets to transmit", number);

		} else {
			log.info("No more chunks available. Stopping OTAP. Done.");
			HandlerTools.sendUpstream(programStatus, context);
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
			}
		}


		for (ISenseOtapDevice device : failedDevicesToRemove) {
			log.warn("Device {} failed (still in chunk {}, current is {}. Removing it.",
					new Object[]{Integer.toHexString(device.getId()), device.getChunkNo(), chunk.getChunkNumber()}
			);

			programStatus.addFailedDevice(device.getId());
			devicesToProgram.remove(device.getId());
		}

		if (chunkTimeout.isTimeout() || isAllDevicesReceivedAllPacketsInChunk()) {
			log.info("Leaping to next chunk. Still remaining packets [" + remainingPacketsInChunk.size() + "]");
			prepareChunk(chunk.getChunkNumber() + 1);
		}
	}

	/**
	 *
	 */
	private boolean isAllDevicesReceivedAllPacketsInChunk() {
		boolean allPacketsAtAllDevicesRX = true;

		for (ISenseOtapDevice d : devicesToProgram.values()) {
			if (!d.isChunkComplete()) {
				allPacketsAtAllDevicesRX = false;
			}
		}

		if (allPacketsAtAllDevicesRX) {
			log.debug("All {} devices received all packets in chunk", devicesToProgram.size());
		}

		return allPacketsAtAllDevicesRX;
	}

	@Override
	public void afterAdd(ChannelHandlerContext ctx) throws Exception {
		checkState(context == null);
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
