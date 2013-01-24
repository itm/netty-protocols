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
package de.uniluebeck.itm.netty.isense.otap.presencedetect;

import de.uniluebeck.itm.netty.isense.otap.ChipType;
import de.uniluebeck.itm.netty.isense.otap.ISenseOtapDevice;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.PresenceDetectReply;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.PresenceDetectRequest;
import de.uniluebeck.itm.netty.util.HandlerTools;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PresenceDetectHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {

	private final Logger log;

	private final ScheduledExecutorService executorService;

	private final int presenceDetectInterval;

	private final TimeUnit presenceDetectIntervalTimeUnit;

	private ChannelHandlerContext context = null;

	private ScheduledFuture<?> sendPresenceDetectRunnableSchedule;

	private final TimedCache<Integer, ISenseOtapDevice> detectedDevices;

	private final Runnable sendPresenceDetectRunnable = new Runnable() {
		@Override
		public void run() {
			if (context != null) {
				log.debug("Sending Presence Detect Request");
				HandlerTools.sendDownstream(new PresenceDetectRequest(), context);
			}
		}
	};

	public PresenceDetectHandler(final String instanceName, final ScheduledExecutorService executorService,
								 final int presenceDetectInterval, int deviceTimeout, final TimeUnit timeunit) {
		log = LoggerFactory.getLogger((instanceName != null) ? instanceName : PresenceDetectHandler.class.getName());

		this.executorService = executorService;
		this.presenceDetectIntervalTimeUnit = timeunit;
		this.presenceDetectInterval = presenceDetectInterval;

		detectedDevices = new TimedCache<Integer, ISenseOtapDevice>(deviceTimeout, timeunit);
	}

	public void startPresenceDetect() {
		log.info("Starting presence detect");
		stopPresenceDetectInternal();
		sendPresenceDetectRunnableSchedule =
				executorService.scheduleWithFixedDelay(sendPresenceDetectRunnable, 0, presenceDetectInterval,
						presenceDetectIntervalTimeUnit
				);

	}

	private void stopPresenceDetectInternal() {
		if (sendPresenceDetectRunnableSchedule != null) {
			sendPresenceDetectRunnableSchedule.cancel(false);
		}
	}

	public void stopPresenceDetect() {
		stopPresenceDetectInternal();
		log.info("Stopped presence detect");
	}

	public Collection<ISenseOtapDevice> getDetectedDevices() {
		return detectedDevices.values();
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		if (e.getMessage() instanceof PresenceDetectControlStart) {
			startPresenceDetect();
		} else if (e.getMessage() instanceof PresenceDetectControlStop) {
			stopPresenceDetect();
		} else {
			super.writeRequested(ctx, e);
		}

	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		Object message = e.getMessage();

		if (!(message instanceof PresenceDetectReply)) {
			super.messageReceived(ctx, e);
			return;
		}

		PresenceDetectReply reply = (PresenceDetectReply) message;
		log.trace("Received presence detect reply from: {}", reply.device_id);

		ISenseOtapDevice d = getOrAddDevice(reply.device_id);

		d.setApplicationID(reply.application_id);
		d.setSoftwareRevision(reply.revision_no);
		d.setChipType(ChipType.getChipType(reply.chip_type));
		d.setProtocolVersion(reply.protocol_version);
		d.getLastReception().touch();

		if (log.isDebugEnabled()) {
			log.debug("Detected {} devices with ids: {}", detectedDevices.size(),
					Arrays.toString(detectedDevices.keySet().toArray())
			);
		}

		ctx.sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), new PresenceDetectStatus(detectedDevices.keySet()),
				ctx.getChannel().getRemoteAddress()
		)
		);
	}

	private ISenseOtapDevice getOrAddDevice(int deviceId) {
		ISenseOtapDevice device = detectedDevices.get(deviceId);
		if (device == null) {
			device = new ISenseOtapDevice();
			device.setId(deviceId);
			detectedDevices.put(deviceId, device);
		}

		return device;
	}

	@Override
	public void afterAdd(ChannelHandlerContext ctx) throws Exception {
		if (context != null) {
			throw new RuntimeException("A single instance may only be added once to a pipeline.");
		}

		this.context = ctx;
	}

	@Override
	public void afterRemove(ChannelHandlerContext ctx) throws Exception {
		this.context = null;
		stopPresenceDetect();
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
