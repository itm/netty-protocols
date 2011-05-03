package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.LoggerFactory;

public class ISenseOtapAutomatedHandler extends SimpleChannelHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ISenseOtapAutomatedHandler.class);

    private enum State {
        IDLE, PRESENCE_DETECT, PROGRAM
    };

    private State state = State.IDLE;
    private PresenceDetectState lastPresenceDetectState;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object message = e.getMessage();
        
        if( message instanceof PresenceDetectState){
            lastPresenceDetectState = (PresenceDetectState) message;
            // if all received or timeout go to programming phase
            
        }

        super.messageReceived(ctx, e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();

        if (message instanceof ISenseOtapProgramRequest) {
            ISenseOtapProgramRequest request = (ISenseOtapProgramRequest) message;

            if (state == State.IDLE) {
                e.getChannel().write(new PresenceDetectControlStart());
                e.getChannel().write(new PresenceDetectControlStop());
                e.getChannel().write(request);
            } else {
                // TODO Inform upstream handlers
                log.error("Already in non-IDLE state {}. Ignoring request.", state);
            }

        } else {
            super.writeRequested(ctx, e);
        }

    }

}
