package de.uniluebeck.itm.netty.handlerstack;

import com.google.common.collect.Lists;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.tr.util.AbstractListenable;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FilterPipelineImpl implements FilterPipeline {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineImpl.class);

	private static class UpstreamListenerManager extends AbstractListenable<FilterPipeline.UpstreamOutputListener> {
		public void notifyListeners(final ChannelBuffer message, final SocketAddress sourceAddress) {
			for (FilterPipeline.UpstreamOutputListener listener : listeners) {
				listener.receiveUpstreamOutput(message, sourceAddress);
			}
		}
	}

	private static class DownstreamListenerManager extends AbstractListenable<FilterPipeline.DownstreamOutputListener> {
		public void notifyListeners(final ChannelBuffer message, final SocketAddress targetAddress) {
			for (FilterPipeline.DownstreamOutputListener listener : listeners) {
				listener.receiveDownstreamOutput(message, targetAddress);
			}
		}
	}

	private class TopHandler extends SimpleChannelUpstreamHandler implements LifeCycleAwareChannelHandler {

		private ChannelHandlerContext ctx;

		@Override
		public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
			upstreamListenerManager.notifyListeners((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
		}

		@Override
		public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
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
		}

		public void sendDownstream(final ChannelBuffer message, final SocketAddress targetAddress) {
			HandlerTools.sendDownstream(message, ctx, targetAddress);
		}
	}

	private class BottomHandler extends SimpleChannelDownstreamHandler implements LifeCycleAwareChannelHandler {

		private ChannelHandlerContext ctx;

		@Override
		public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
			downstreamListenerManager.notifyListeners((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
		}

		@Override
		public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
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
		}

		public void sendUpstream(final ChannelBuffer message, final SocketAddress sourceAddress) {
			HandlerTools.sendUpstream(message, ctx, sourceAddress);
		}
	}

	private HandlerStack handlerStack = new HandlerStack();

	private UpstreamListenerManager upstreamListenerManager = new UpstreamListenerManager();

	private DownstreamListenerManager downstreamListenerManager = new DownstreamListenerManager();

	private TopHandler topHandler = new TopHandler();

	private BottomHandler bottomHandler = new BottomHandler();

	public FilterPipelineImpl() {
		handlerStack.setLeftHandler(topHandler);
		handlerStack.setRightHandler(bottomHandler);
		handlerStack.setHandlerStack(Lists.<Tuple<String,ChannelHandler>>newArrayList());
		handlerStack.performSetup();
	}

	@Override
	public void sendDownstream(final ChannelBuffer message, final SocketAddress targetAddress) {
		topHandler.sendDownstream(message, targetAddress);
	}

	@Override
	public void sendUpstream(final ChannelBuffer message, final SocketAddress sourceAddress) {
		bottomHandler.sendUpstream(message, sourceAddress);
	}

	@Override
	public void setChannelPipeline(final List<Tuple<String, ChannelHandler>> handlerStack) {
		try {
			this.handlerStack.setHandlerStack(handlerStack);
			this.handlerStack.performSetup();
		} catch (Exception e) {
			log.error("Exception while setting handler stack: " + e, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addListener(final FilterPipeline.DownstreamOutputListener listener) {
		downstreamListenerManager.addListener(listener);
	}

	@Override
	public void addListener(final FilterPipeline.UpstreamOutputListener listener) {
		upstreamListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(final FilterPipeline.DownstreamOutputListener listener) {
		downstreamListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(final FilterPipeline.UpstreamOutputListener listener) {
		upstreamListenerManager.addListener(listener);
	}
}
