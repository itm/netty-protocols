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

import com.coalesenses.binaryimage.BinaryImage;
import com.coalesenses.tools.iSenseAes128BitKey;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.init.ISenseOtapInitResult;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.init.ISenseOtapInitStartCommand;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectControlStart;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectControlStop;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectStatus;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramResult;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.tr.util.StringUtils;
import de.uniluebeck.itm.tr.util.TimeDiff;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ISenseOtapAutomatedProgrammingHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {
    private final org.slf4j.Logger log;

    private enum State {
        IDLE, PRESENCE_DETECT, OTAP_INIT, OTAP_PROGRAM
    }

    ;

    private ChannelHandlerContext context;
    private State state = State.IDLE;
    private ISenseOtapAutomatedProgrammingRequest programRequest;
    private TimeDiff presenceDetectStart;

    public ISenseOtapAutomatedProgrammingHandler(String instanceName) {
        log =
                LoggerFactory.getLogger((instanceName != null) ? instanceName
                        : ISenseOtapAutomatedProgrammingHandler.class.getName());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof PresenceDetectStatus) {

            if (state == State.PRESENCE_DETECT) {
                PresenceDetectStatus result = (PresenceDetectStatus) message;
                boolean isDetectedAllDevices =
                        result.getDetectedDevices().containsAll(programRequest.getDevicesToProgram());
                boolean isTimeout = presenceDetectStart.isTimeout();

                if (isDetectedAllDevices || isTimeout) {
                    log.info("Detected all devices ({}) or timeout ({}).", isDetectedAllDevices, isTimeout);
                    switchToInitMode((PresenceDetectStatus) message);
                }

            } else {
                log.warn("Ignoring presence detect status update in state {}", state);
            }

        } else if (message instanceof ISenseOtapInitResult) {

            if (state == State.OTAP_INIT) {
                switchToProgrammingMode((ISenseOtapInitResult) message);
            } else {
                log.warn("Ignoring otap init status update in state {}", state);
            }

        } else if (message instanceof ISenseOtapProgramResult) {

            if (state == State.OTAP_PROGRAM) {
                switchToIdleState((ISenseOtapProgramResult) message);
            } else {
                log.warn("Ignoring otap init status update in state {}", state);
            }

        } else {
            super.messageReceived(ctx, e);
        }

    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof ISenseOtapAutomatedProgrammingRequest) {

            if (state == State.IDLE) {
                ISenseOtapAutomatedProgrammingRequest request = (ISenseOtapAutomatedProgrammingRequest) message;

                log.info("Received automated programming request switching to presence detect mode.");
                log.info("Automated programming request: {}", request);
                switchToPresenceDetectMode(request);
            } else {
                // TODO Inform upstream handlers
                log.error("Already in non-IDLE state {}. Ignoring request.", state);
            }

        } else {
            super.writeRequested(ctx, e);
        }

    }

    private void switchToPresenceDetectMode(ISenseOtapAutomatedProgrammingRequest request) {
        state = State.PRESENCE_DETECT;

        this.programRequest = request;
        this.presenceDetectStart = new TimeDiff(request.getPresenceDetectTimeoutAsDurationPlusUnit().toMillis());

        HandlerTools.sendDownstream(new PresenceDetectControlStart(), context);
    }

    private void switchToInitMode(PresenceDetectStatus message) {
        log.info("Switching to otap init mode (after presence detect)");
        state = State.OTAP_INIT;

        HandlerTools.sendDownstream(new PresenceDetectControlStop(), context);

        Set<Integer> detectedDevices = message.getDetectedDevices();
        StringBuilder builder = new StringBuilder();
        for (Integer deviceid : detectedDevices) {
            builder.append("0x");
            builder.append(Integer.toHexString(deviceid));
            builder.append(",");
        }


        log.info("Detected devices: {}", builder.toString());

        Set<Integer> detectedDevicesToProgram =
                Sets.intersection(detectedDevices, programRequest.getDevicesToProgram());

        BinaryImage program = new BinaryImage(programRequest.getOtapProgram());

        ISenseOtapInitStartCommand command =
                new ISenseOtapInitStartCommand(detectedDevicesToProgram, programRequest.getOtapInitTimeoutAsDurationPlusUnit(),
                        program.getChunkCount(), programRequest.getMaxRerequests(),
                        programRequest.getTimeoutMultiplier());

        HandlerTools.sendDownstream(command, context);
    }

    private void switchToProgrammingMode(ISenseOtapInitResult otapInitResult) {
        if (programRequest == null || context == null) {
            log.warn("Program request {} or context {} is null. Doing nothing.", programRequest, context);
            return;
        }

        state = State.OTAP_PROGRAM;
        log.info("Switching to programming mode. {} devices out of {} selected initialized.", otapInitResult
                .getInitializedDevices().size(), programRequest.getDevicesToProgram().size());

        if (log.isDebugEnabled())
            log.debug("Devices that are now initialized are: {}",
                    StringUtils.toString(otapInitResult.getInitializedDevices(), ", "));

        ISenseOtapProgramRequest request =
                new ISenseOtapProgramRequest(otapInitResult.getInitializedDevices(), programRequest.getOtapProgram());

        // Select the desired AES encryption/decryption
        iSenseAes128BitKey aesKeyAsISenseAes128BitKey = programRequest.getAesKeyAsISenseAes128BitKey();
        if (aesKeyAsISenseAes128BitKey != null) {
            HandlerTools.sendDownstream(new ISenseOtapPacketEncoderSetAESKeyRequest(aesKeyAsISenseAes128BitKey), context);
        }

        // Send the programming request downstream
        HandlerTools.sendDownstream(request, context);
    }

    private void switchToIdleState(ISenseOtapProgramResult message) {
        if (programRequest == null) {
            log.error("No current program request. Ignoring invocation.");
        }

        log.info("Programming done or timed out (selected: {}, done: {}, failed: {}), switching to idle state",
                new Object[]{message.getDevicesToBeProgrammed().size(), message.getDoneDevices().size(),
                        message.getFailedDevices().size()});

        state = State.IDLE;
        programRequest = null;

        // Disable AES encryption
        HandlerTools.sendDownstream(new ISenseOtapPacketEncoderSetAESKeyRequest(null), context);
        HandlerTools.sendDownstream(new ISenseOtapPacketDecoderSetAESKeyRequest(null), context);

        // Send result upstream
        HandlerTools.sendUpstream(message, context);
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
