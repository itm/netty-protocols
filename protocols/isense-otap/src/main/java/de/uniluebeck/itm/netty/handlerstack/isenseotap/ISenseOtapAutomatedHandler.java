package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.LifeCycleAwareChannelHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.tr.util.StringUtils;
import de.uniluebeck.itm.tr.util.TimeDiff;

public class ISenseOtapAutomatedHandler extends SimpleChannelHandler implements LifeCycleAwareChannelHandler {
    private final org.slf4j.Logger log;

    private enum State {
        IDLE, PRESENCE_DETECT, PROGRAM
    };

    private ChannelHandlerContext context;
    private State state = State.IDLE;

    private final TimeDiff presenceDetectStart;
    private PresenceDetectState lastPresenceDetectState;
    private final TimeDiff programmingStart;

    private ISenseOtapProgramRequest programRequest;

    public ISenseOtapAutomatedHandler(long presenceDetectTimeout, TimeUnit presenceDetectTimeoutTimeUnit,
            long programmingTimeout, TimeUnit programmingTimeoutTimeUnit) {
        this(null, presenceDetectTimeout, presenceDetectTimeoutTimeUnit, programmingTimeout, programmingTimeoutTimeUnit);
    }

    public ISenseOtapAutomatedHandler(String instanceName, long presenceDetectTimeout,
            TimeUnit presenceDetectTimeoutTimeUnit, long programmingTimeout, TimeUnit programmingTimeoutTimeUnit) {
        super();

        log = LoggerFactory.getLogger((instanceName != null) ? instanceName : ISenseOtapHandler.class.getName());
        presenceDetectStart = new TimeDiff(presenceDetectTimeoutTimeUnit.toMillis(presenceDetectTimeout));
        programmingStart = new TimeDiff(programmingTimeoutTimeUnit.toMillis(programmingTimeout));
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof PresenceDetectState && state == State.PRESENCE_DETECT) {
            lastPresenceDetectState = (PresenceDetectState) message;
            checkSwitchToProgrammingMode();
        } else if (message instanceof ISenseOtapProgramState && state == State.PROGRAM) {
            checkSwitchToIdleState((ISenseOtapProgramState) message);
        } else {
            super.messageReceived(ctx, e);
        }

    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof ISenseOtapProgramRequest) {

            if (state == State.IDLE) {
                log.info("Received programming request {}, switching to presence detect first.");
                programRequest = (ISenseOtapProgramRequest) message;
                state = State.PRESENCE_DETECT;
                presenceDetectStart.touch();
                HandlerTools.sendDownstream(new PresenceDetectControlStart(), context);
            } else {
                // TODO Inform upstream handlers
                log.error("Already in non-IDLE state {}. Ignoring request.", state);
            }

        } else {
            super.writeRequested(ctx, e);
        }

    }

    private void checkSwitchToIdleState(ISenseOtapProgramState message) {
        if (programRequest == null) {
            log.error("No current program request. Ignoring invocation.");
        }

        boolean done = message.isDone();
        boolean timeout = programmingStart.isTimeout();

        if (done || timeout) {
            log.info("Programming done {} or timed out {}, switching to idle state", done, timeout);
            state = State.IDLE;
            programRequest = null;
            HandlerTools.sendUpstream(message, context);
        }

    }

    private void checkSwitchToProgrammingMode() {
        if (programRequest == null || lastPresenceDetectState == null || context == null) {
            log.warn("Program request {}, last presence detect state {} or context {} is null. Doing nothing.",
                    new Object[] { programRequest, lastPresenceDetectState, context });
            return;
        }

        // If replies received from all devices or on timeout go to programming phase
        boolean allDevicesDetected =
                lastPresenceDetectState.getDetectedDevices().containsAll(programRequest.getOtapDevices());
        boolean presenceDetectTimeout = presenceDetectStart.isTimeout();

        if (allDevicesDetected || presenceDetectTimeout) {
            log.info("Switching to programming mode. All devices detected {} or presence detect timeout {}",
                    allDevicesDetected, presenceDetectTimeout);

            if (log.isDebugEnabled())
                log.debug("Devices detected: {}",
                        StringUtils.toString(lastPresenceDetectState.getDetectedDevices(), ", "));

            state = State.PROGRAM;
            programmingStart.touch();

            ISenseOtapProgramRequest modifiedRequest =
                    new ISenseOtapProgramRequest(lastPresenceDetectState.getDetectedDevices(),
                            programRequest.getOtapProgram());

            HandlerTools.sendDownstream(new PresenceDetectControlStop(), context);
            HandlerTools.sendDownstream(modifiedRequest, context);
        }

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
