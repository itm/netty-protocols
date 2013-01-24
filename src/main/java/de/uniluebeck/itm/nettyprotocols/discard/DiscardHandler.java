package de.uniluebeck.itm.nettyprotocols.discard;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class DiscardHandler extends SimpleChannelHandler {

	private final boolean discardUpstream;

	private final boolean discardDownstream;

	public DiscardHandler(boolean discardUpstream, boolean discardDownstream) {
		this.discardUpstream = discardUpstream;
		this.discardDownstream = discardDownstream;
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		if (!discardDownstream) {
			super.writeRequested(ctx, e);
		}
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		if (!discardUpstream) {
			super.messageReceived(ctx, e);
		}
	}
}
