package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jboss.netty.channel.*;

import java.util.Map;

public class NodeAPIHandler extends SimpleChannelHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

	@Inject
	@Named("requestCache")
	private Map<Long, Request> requestCache;

	NodeAPIHandler() {
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		DownstreamMessageEvent event = (DownstreamMessageEvent) e;
		final SetVirtualLinkRequest request = (SetVirtualLinkRequest) event.getMessage();
		final long destinationNode = request.getDestinationNode();
		final SetVirtualLinkCommand setVirtualLinkCommand = new SetVirtualLinkCommand(destinationNode, 1234);
		final DownstreamMessageEvent downstreamMessageEvent = new DownstreamMessageEvent(
				event.getChannel(),
				event.getFuture(),
				setVirtualLinkCommand,
				event.getRemoteAddress()
		);
		ctx.sendDownstream(downstreamMessageEvent);
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		// TODO implement
	}
}
