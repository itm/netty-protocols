package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import java.net.SocketAddress;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class FilterHandler extends SimpleChannelHandler implements
		FilterPipeline.DownstreamOutputListener,
		FilterPipeline.UpstreamOutputListener,
		LifeCycleAwareChannelHandler {

	private FilterPipeline filterPipeline;

	private ChannelHandlerContext ctx;

	public FilterHandler(final FilterPipeline filterPipeline) {
		checkNotNull(filterPipeline);
		this.filterPipeline = filterPipeline;
		this.filterPipeline.addListener((FilterPipeline.DownstreamOutputListener) this);
		this.filterPipeline.addListener((FilterPipeline.UpstreamOutputListener) this);
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		filterPipeline.sendDownstream((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		filterPipeline.sendUpstream((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
	}

	@Override
	public void receiveDownstreamOutput(final ChannelBuffer message, final SocketAddress targetAddress) {
		checkState(ctx != null);
		HandlerTools.sendDownstream(message, ctx, targetAddress);
	}

	@Override
	public void downstreamExceptionCaught(final Throwable e) {
		ctx.sendUpstream(new DefaultExceptionEvent(ctx.getChannel(), e));
	}

	@Override
	public void receiveUpstreamOutput(final ChannelBuffer message, final SocketAddress sourceAddress) {
		checkState(ctx != null);
		HandlerTools.sendUpstream(message, ctx, sourceAddress);
	}

	@Override
	public void upstreamExceptionCaught(final Throwable e) {
		ctx.sendUpstream(new DefaultExceptionEvent(ctx.getChannel(), e));
	}

	@Override
	public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}

	@Override
	public void afterAdd(final ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	@Override
	public void beforeRemove(final ChannelHandlerContext ctx) throws Exception {
		this.ctx = null;
	}

	@Override
	public void afterRemove(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}
}
