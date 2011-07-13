package de.uniluebeck.itm.netty.handlerstack.discard;

import org.jboss.netty.channel.*;

public class DiscardMessagesHandler extends SimpleChannelHandler {

	private final boolean discardUpstream;

	private final boolean discardDownstream;

	public DiscardMessagesHandler(boolean discardUpstream, boolean discardDownstream) {
		this.discardUpstream = discardUpstream;
		this.discardDownstream = discardDownstream;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		if (discardDownstream) {
			// discard message
		} else {
			super.writeRequested(ctx, e);
		}
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		if (discardUpstream) {
			// discard message
		} else {
			super.messageReceived(ctx, e);
		}
	}
}
