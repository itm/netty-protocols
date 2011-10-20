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
package de.uniluebeck.itm.netty.handlerstack.isenseotap.init;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitReply;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapInitRequest;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.tr.util.StringUtils;
import de.uniluebeck.itm.tr.util.TimeDiff;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ISenseOtapInitHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {
    private final Logger log;
    private final int maxDevicesPerPacket = new OtapInitRequest().participating_devices.value.length;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private ISenseOtapInitStartCommand request = null;
    private ChannelHandlerContext context;
    private TimeDiff otapInitStart;

    /**
     * A set of devices selected to program, they must all ack before they are programmed
     */
    private Set<Integer> initializedDevices;

    private ScheduledFuture<?> sendOtapInitRequestsSchedule;
    private final Runnable sendOtapInitRequestsRunnable = new Runnable() {

        private OtapInitRequest createRequest() {
            Preconditions.checkNotNull(request, "No otap init request has been received");

            OtapInitRequest req = new OtapInitRequest();
            req.chunk_count = request.getChunkCount();
            req.max_re_requests = request.getMaxRerequests();
            req.timeout_multiplier_ms = request.getTimeoutMultiplier();

            return req;
        }

        @Override
        public void run() {
            if (context == null)
                return;

            OtapInitRequest req = createRequest();

            // Send a number of otap init requests with maxDevicesPerPacket devices per request (size issue)
            int currentDeviceCount = 0;
            for (Integer id : request.getDevicesToInitialize()) {
                req.participating_devices.value[currentDeviceCount] = id;
                req.participating_devices.count++;
                currentDeviceCount++;

                if (currentDeviceCount == maxDevicesPerPacket) {
                    log.trace("Sending otap init request");
                    HandlerTools.sendDownstream(req, context);
                    currentDeviceCount = 0;
                    req = createRequest();
                }
            }

            // Send the last packet (if there is one)
            if (currentDeviceCount > 0 && currentDeviceCount < maxDevicesPerPacket) {
                log.trace("Sending otap init request: {}", req);
                HandlerTools.sendDownstream(req, context);

            }

            checkIfDoneAndNotifyUpstream();
        }

    };

    public ISenseOtapInitHandler() {
        this(null);
    }

    public ISenseOtapInitHandler(String instanceName) {
        log = LoggerFactory.getLogger((instanceName != null) ? instanceName : ISenseOtapInitHandler.class.getName());
    }

    public void startOtapInit(ISenseOtapInitStartCommand req) {
        stopOtapInit();

        this.request = req;

        long transmitFrequencyMillis = Math.max(request.getOtapInitTimeout().toMillis() / 500, 100);

        initializedDevices = Sets.newHashSet();

        sendOtapInitRequestsSchedule =
                executorService.scheduleWithFixedDelay(sendOtapInitRequestsRunnable, 0, transmitFrequencyMillis,
                        TimeUnit.MILLISECONDS);

        otapInitStart = new TimeDiff(request.getOtapInitTimeout().toMillis());
    }

    public void stopOtapInit() {
        if (sendOtapInitRequestsSchedule != null)
            sendOtapInitRequestsSchedule.cancel(false);

        initializedDevices = null;
        request = null;
        otapInitStart = null;
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof ISenseOtapInitStartCommand) {
            log.debug("Received otap init request. Starting to initialize devices...");
            ISenseOtapInitStartCommand request = (ISenseOtapInitStartCommand) message;
            startOtapInit(request);

        } else {
            super.writeRequested(ctx, e);
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof OtapInitReply) {
            OtapInitReply reply = (OtapInitReply) message;

            if (request != null) {
                handleOtapInitReply(reply);
            } else {
                log.debug("Ignoring otap init reply in wrong state --> no previous request");
            }

        } else {
            super.messageReceived(ctx, e);
        }

    }

    private void handleOtapInitReply(OtapInitReply reply) {
        Set<Integer> devicesToInitialize = request.getDevicesToInitialize();

        if (devicesToInitialize.contains(reply.device_id)) {
            initializedDevices.add(reply.device_id);
            log.trace("Init reply from device {}, now got {} devices: {}", new Object[]{reply.device_id,
                    initializedDevices.size(), StringUtils.toString(initializedDevices, ", ")});
        } else {
            log.trace("Ignored unsolicited reply from device {} that does not participate.",
                    StringUtils.toHexString(reply.device_id));
        }

        checkIfDoneAndNotifyUpstream();
    }

    /**
     * Check if all devices have sent acks or if a timeout has occured.
     */
    private void checkIfDoneAndNotifyUpstream() {
        Set<Integer> devicesToInitialize = request.getDevicesToInitialize();
        boolean timeout = otapInitStart.isTimeout();
        boolean allDevicesInitialized = devicesToInitialize.size() == initializedDevices.size();

        if (timeout || allDevicesInitialized) {

            log.info("All {} devices have either acknowledged {}, or a timeout occured {}. Done", new Object[]{
                    devicesToInitialize.size(), allDevicesInitialized, timeout});

            ISenseOtapInitResult result = new ISenseOtapInitResult(request, initializedDevices);

            stopOtapInit();

            HandlerTools.sendUpstream(result, context);
        }
    }

    @Override
    public void afterAdd(ChannelHandlerContext ctx) throws Exception {
        Preconditions.checkArgument(context == null, "This instance may only be inserted once into a pipeline. ");
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
