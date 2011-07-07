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
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.jboss.netty.channel.Channels.pipeline;

public class FilterPipelineImpl implements FilterPipeline {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineImpl.class);

	private static class DummyChannel extends AbstractChannel {

		private static final Integer DUMMY_ID = 0;

		private final ChannelConfig config;

		private final SocketAddress localAddress = new SocketAddress() {
		};

		private final SocketAddress remoteAddress = new SocketAddress() {
		};

		public DummyChannel(ChannelPipeline pipeline, ChannelSink sink) {
			super(DUMMY_ID, null, null, pipeline, sink);
			config = new DefaultChannelConfig();
		}

		public ChannelConfig getConfig() {
			return config;
		}

		public SocketAddress getLocalAddress() {
			return localAddress;
		}

		public SocketAddress getRemoteAddress() {
			return remoteAddress;
		}

		public boolean isBound() {
			return true;
		}

		public boolean isConnected() {
			return true;
		}

	}

	private final class DummyChannelSink implements ChannelSink {

		public DummyChannelSink() {
			super();
		}

		public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) {
			// do nothing
		}

		public void exceptionCaught(
				ChannelPipeline pipeline, ChannelEvent e,
				ChannelPipelineException cause) throws Exception {
			throw new RuntimeException(cause);
		}

	}

	private static class UpstreamListenerManager extends AbstractListenable<FilterPipeline.UpstreamOutputListener> {

		public void receiveUpstreamOutput(final ChannelBuffer message, final SocketAddress sourceAddress) {
			for (FilterPipeline.UpstreamOutputListener listener : listeners) {
				listener.receiveUpstreamOutput(message, sourceAddress);
			}
		}

		public void upstreamExceptionCaught(final Throwable e) {
			for (FilterPipeline.UpstreamOutputListener listener : listeners) {
				listener.upstreamExceptionCaught(e);
			}
		}
	}

	private static class DownstreamListenerManager extends AbstractListenable<FilterPipeline.DownstreamOutputListener> {

		public void receiveDownstreamOutput(final ChannelBuffer message, final SocketAddress targetAddress) {
			for (FilterPipeline.DownstreamOutputListener listener : listeners) {
				listener.receiveDownstreamOutput(message, targetAddress);
			}
		}

		public void downstreamExceptionCaught(final Throwable e) {
			for (FilterPipeline.DownstreamOutputListener listener : listeners) {
				listener.downstreamExceptionCaught(e);
			}
		}
	}

	private class TopHandler extends SimpleChannelUpstreamHandler implements LifeCycleAwareChannelHandler {

		private ChannelHandlerContext ctx;

		@Override
		public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
			try {
				upstreamListenerManager.receiveUpstreamOutput((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
			} catch (Exception e1) {
				upstreamListenerManager.upstreamExceptionCaught(e1);
			}
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

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
			upstreamListenerManager.upstreamExceptionCaught(e.getCause());
		}
	}

	private class BottomHandler extends SimpleChannelDownstreamHandler implements LifeCycleAwareChannelHandler {

		private ChannelHandlerContext ctx;

		@Override
		public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
			try {
				downstreamListenerManager.receiveDownstreamOutput((ChannelBuffer) e.getMessage(), e.getRemoteAddress());
			} catch (Exception e1) {
				downstreamListenerManager.downstreamExceptionCaught(e1);
			}
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

	private UpstreamListenerManager upstreamListenerManager = new UpstreamListenerManager();

	private DownstreamListenerManager downstreamListenerManager = new DownstreamListenerManager();

	private List<Tuple<String, ChannelHandler>> channelPipeline = newArrayList();

	private TopHandler topHandler = new TopHandler();

	private BottomHandler bottomHandler = new BottomHandler();

	@SuppressWarnings("unused")
	private ChannelPipeline pipeline;

	public FilterPipelineImpl() {
		setChannelPipeline(null);
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
	public void setChannelPipeline(final List<Tuple<String, ChannelHandler>> newChannelPipeline) {

		final ChannelPipeline newPipeline = pipeline();

		if (newChannelPipeline != null) {
			for (Tuple<String, ChannelHandler> tuple : newChannelPipeline) {
				newPipeline.addFirst(tuple.getFirst(), tuple.getSecond());
			}
		}

		newPipeline.addLast("topHandler", topHandler);
		newPipeline.addFirst("bottomHandler", bottomHandler);

		final DummyChannelSink channelSink = new DummyChannelSink();
		new DummyChannel(newPipeline, channelSink);

		for (Tuple<String, ChannelHandler> handlerTuple : channelPipeline) {
			final ChannelHandler oldHandler = handlerTuple.getSecond();
			if (oldHandler instanceof LifeCycleAwareChannelHandler) {
				final ChannelHandlerContext context = pipeline.getContext(oldHandler);
				try {
					((LifeCycleAwareChannelHandler) oldHandler).beforeRemove(context);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}

		final ChannelPipeline oldPipeline = pipeline;
		final List<Tuple<String, ChannelHandler>> oldChannelPipeline = channelPipeline;

		pipeline = newPipeline;
		channelPipeline = newChannelPipeline == null ?
				Lists.<Tuple<String, ChannelHandler>>newArrayList() :
				newChannelPipeline;

		for (Tuple<String, ChannelHandler> handlerTuple : oldChannelPipeline) {
			final ChannelHandler oldHandler = handlerTuple.getSecond();
			if (oldHandler instanceof LifeCycleAwareChannelHandler) {
				final ChannelHandlerContext context = oldPipeline.getContext(oldHandler);
				try {
					((LifeCycleAwareChannelHandler) oldHandler).afterRemove(context);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}


	}

	@Override
	public List<Tuple<String, ChannelHandler>> getChannelPipeline() {
		return channelPipeline;
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
