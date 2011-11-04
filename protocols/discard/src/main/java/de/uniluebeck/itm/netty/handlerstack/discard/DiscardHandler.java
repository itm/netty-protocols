package de.uniluebeck.itm.netty.handlerstack.discard;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelHandler;

public class DiscardHandler extends SimpleChannelHandler {

	private final boolean discardUpstream;

	private final boolean discardDownstream;

	public DiscardHandler(boolean discardUpstream, boolean discardDownstream) {
		this.discardUpstream = discardUpstream;
		this.discardDownstream = discardDownstream;
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (!discardUpstream) {
			super.handleUpstream(ctx, e);
		}
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (!discardDownstream) {
			super.handleDownstream(ctx, e);
		}
	}
}
