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

import java.util.Arrays;
import java.util.Collection;
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

import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.PresenceDetectReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.PresenceDetectRequest;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.wsn.devicedrivers.generic.ChipType;

public class PresenceDetectHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(PresenceDetectHandler.class);

    private final ScheduledExecutorService executorService;

    private final int presenceDetectInterval;

    private final TimeUnit presenceDetectIntervalTimeunit;

    private Channel channel = null;

    private ScheduledFuture<?> sendPresenceDetectRunnableSchedule;

    private final TimedCache<Integer, OtapDevice> detectedDevices;

    private final Runnable sendPresenceDetectRunnable = new Runnable() {
        @Override
        public void run() {
            if (channel != null) {
                channel.write(new PresenceDetectRequest());
                log.trace("Sent Presence Detect Request");
            }
        }
    };

    
    public PresenceDetectHandler(final ScheduledExecutorService executorService, final int presenceDetectInterval,
            int deviceTimeout, final TimeUnit timeunit) {
        this.executorService = executorService;
        this.presenceDetectIntervalTimeunit = timeunit;
        this.presenceDetectInterval = presenceDetectInterval;

        detectedDevices = new TimedCache<Integer, OtapDevice>(deviceTimeout, timeunit);
    }

    public void startPresenceDetect() {
        stopPresenceDetect();
        sendPresenceDetectRunnableSchedule =
                executorService.scheduleWithFixedDelay(sendPresenceDetectRunnable, 0, presenceDetectInterval,
                        presenceDetectIntervalTimeunit);

    }

    public void stopPresenceDetect() {
        sendPresenceDetectRunnableSchedule.cancel(false);
    }

    public Collection<OtapDevice> getDetectedDevices() {
        return detectedDevices.values();
    }
    
    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        stopPresenceDetect();
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

        if (!(message instanceof PresenceDetectReply)) {
            super.messageReceived(ctx, e);
            return;
        }

        PresenceDetectReply reply = (PresenceDetectReply) message;
        log.debug("Received presence detect reply: {}", reply);

        OtapDevice d = getOrCreateDevice(reply.device_id);

        d.setApplicationID(reply.application_id);
        d.setSoftwareRevision(reply.revision_no);
        d.setChipType(ChipType.getChipType(reply.chip_type));
        d.setProtocolVersion(reply.protocol_version);
        d.getLastReception().touch();
        
        if( log.isDebugEnabled())
            log.debug("Detected {} devices with ids: {}", detectedDevices.size(), Arrays.toString(detectedDevices.keySet().toArray()));
    }

    private OtapDevice getOrCreateDevice(int deviceId) {
        OtapDevice device = detectedDevices.get(deviceId);
        if (device == null)
            device = new OtapDevice();

        return device;
    }
}
