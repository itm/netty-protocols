package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class FilterPipelineImpl implements FilterPipeline {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineImpl.class);

	private class FilterPipelineChannelHandlerContext implements ChannelHandlerContext {

		volatile FilterPipelineChannelHandlerContext upperContext;

		volatile FilterPipelineChannelHandlerContext lowerContext;

		private final String name;

		private final ChannelHandler handler;

		private final boolean canHandleUpstream;

		private final boolean canHandleDownstream;

		private volatile Object attachment;

		FilterPipelineChannelHandlerContext(final String name, final ChannelHandler handler) {

			if (name == null) {
				throw new NullPointerException("name");
			}

			if (handler == null) {
				throw new NullPointerException("handler");
			}

			canHandleUpstream = handler instanceof ChannelUpstreamHandler;
			canHandleDownstream = handler instanceof ChannelDownstreamHandler;

			if (!canHandleUpstream && !canHandleDownstream) {
				throw new IllegalArgumentException(
						"handler must be either " +
								ChannelUpstreamHandler.class.getName() + " or " +
								ChannelDownstreamHandler.class.getName() + '.'
				);
			}

			this.name = name;
			this.handler = handler;
		}

		public void setLowerContext(final FilterPipelineChannelHandlerContext lowerContext) {
			this.lowerContext = lowerContext;
		}

		public void setUpperContext(final FilterPipelineChannelHandlerContext upperContext) {
			this.upperContext = upperContext;
		}

		public FilterPipelineChannelHandlerContext getLowerContext() {
			return lowerContext;
		}

		public FilterPipelineChannelHandlerContext getUpperContext() {
			return upperContext;
		}

		@Override
		public Channel getChannel() {
			return getPipeline().getChannel();
		}

		@Override
		public ChannelPipeline getPipeline() {
			return FilterPipelineImpl.this;
		}

		@Override
		public boolean canHandleDownstream() {
			return canHandleDownstream;
		}

		@Override
		public boolean canHandleUpstream() {
			return canHandleUpstream;
		}

		@Override
		public ChannelHandler getHandler() {
			return handler;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Object getAttachment() {
			return attachment;
		}

		@Override
		public void setAttachment(Object attachment) {
			this.attachment = attachment;
		}

		@Override
		public void sendDownstream(ChannelEvent e) {

			// if we are at the bottom of the stack
			if (lowerContext == null) {

				if (outerContext != null) {
					outerContext.sendDownstream(e);
				}
			}

			// if there's at least one handler beneath us
			else {
				try {

					((ChannelDownstreamHandler) lowerContext.getHandler()).handleDownstream(lowerContext, e);

				} catch (Exception e1) {

					try {
						((ChannelDownstreamHandler) lowerContext.getHandler()).handleDownstream(
								lowerContext,
								new DefaultExceptionEvent(lowerContext.getChannel(), e1)
						);
					} catch (Exception e2) {
						bottomHandler.exceptionCaught(
								outerContext,
								new DefaultExceptionEvent(outerContext.getChannel(), e2)
						);
					}
				}
			}
		}

		@Override
		public void sendUpstream(ChannelEvent e) {

			if (upperContext == null) {

				if (outerContext != null) {
					outerContext.sendUpstream(e);
				}
			}

			else {

				try {

					((ChannelUpstreamHandler) upperContext.getHandler()).handleUpstream(upperContext, e);

				} catch (Exception e1) {

					try {
						((ChannelUpstreamHandler) upperContext.getHandler()).handleUpstream(
								upperContext,
								new DefaultExceptionEvent(upperContext.getChannel(), e1)
						);
					} catch (Exception e2) {
						topHandler.exceptionCaught(
								outerContext,
								new DefaultExceptionEvent(outerContext.getChannel(), e2)
						);
					}
				}
			}
		}

	}

	private class FilterPipelineChannel extends AbstractChannel {

		private final ChannelConfig config;

		private final SocketAddress localAddress = new SocketAddress() {
		};

		private final SocketAddress remoteAddress = new SocketAddress() {
		};

		public FilterPipelineChannel(ChannelPipeline pipeline, org.jboss.netty.channel.ChannelSink sink) {
			super(0, null, null, pipeline, sink);
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
			return outerContext != null;
		}

		public boolean isConnected() {
			return outerContext != null;
		}

	}

	private class ChannelSink implements org.jboss.netty.channel.ChannelSink {

		public ChannelSink() {
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

	private static class UpstreamListenerManager extends ListenerManagerImpl<FilterPipelineUpstreamListener>
			implements FilterPipelineUpstreamListener {

		@Override
		public void upstreamExceptionCaught(final Throwable e) {

			for (FilterPipelineUpstreamListener listener : listeners) {
				listener.upstreamExceptionCaught(e);
			}
		}

		@Override
		public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) {

			for (FilterPipelineUpstreamListener listener : listeners) {

				try {
					listener.handleUpstream(ctx, e);
				} catch (Exception e1) {
					log.error(
							"The FilterPipelineUpstreamListener {} throw the following exception on calling handleUpstream(): {}",
							listener, e1
					);
				}
			}
		}

	}

	private static class DownstreamListenerManager extends ListenerManagerImpl<FilterPipelineDownstreamListener>
			implements FilterPipelineDownstreamListener {

		public void downstreamExceptionCaught(final Throwable e) {

			for (FilterPipelineDownstreamListener listener : listeners) {
				listener.downstreamExceptionCaught(e);
			}
		}

		public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) {
			for (FilterPipelineDownstreamListener listener : listeners) {
				try {
					listener.handleDownstream(ctx, e);
				} catch (Exception e1) {
					log.error(
							"The FilterPipelineDownstreamListener {} throw the following exception on calling handleDownstream(): {}",
							listener, e1
					);
				}
			}
		}

	}

	private class TopHandler extends SimpleChannelHandler {

		@Override
		public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) {

			// pass message to all listeners
			try {
				upstreamListenerManager.handleUpstream(ctx, e);
			} catch (Exception e1) {
				upstreamListenerManager.upstreamExceptionCaught(e1);
			}

			// in case we ourselves are directly inside a pipeline also send it up the stream
			try {
				super.handleUpstream(ctx, e);
			} catch (Exception e1) {
				upstreamListenerManager.upstreamExceptionCaught(e1);
			}
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
			upstreamListenerManager.upstreamExceptionCaught(e.getCause());
			try {
				super.exceptionCaught(ctx, e);
			} catch (Exception e1) {
				upstreamListenerManager.upstreamExceptionCaught(e1);
			}
		}

	}

	private class BottomHandler extends SimpleChannelHandler {

		@Override
		public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) {

			// pass message to all listeners
			try {
				downstreamListenerManager.handleDownstream(ctx, e);
			} catch (Exception e1) {
				downstreamListenerManager.downstreamExceptionCaught(e1);
			}

			// in case we ourselves are directly inside a pipeline also send it down the stream
			try {
				super.handleDownstream(ctx, e);
			} catch (Exception e1) {
				downstreamListenerManager.downstreamExceptionCaught(e1);
			}
		}

		@Override
		public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
			downstreamListenerManager.downstreamExceptionCaught(e.getCause());
			try {
				super.exceptionCaught(ctx, e);
			} catch (Exception e1) {
				downstreamListenerManager.downstreamExceptionCaught(e1);
			}
		}

	}

	private final UpstreamListenerManager upstreamListenerManager = new UpstreamListenerManager();

	private final DownstreamListenerManager downstreamListenerManager = new DownstreamListenerManager();

	private final TopHandler topHandler;

	private final BottomHandler bottomHandler;

	private org.jboss.netty.channel.ChannelSink channelSink;

	private Channel channel;

	/**
	 * If this FilterPipeline instance is placed inside another pipeline this field will hold the "outer context",
	 * i.e. the context of the filter pipeline in the pipeline in which it is placed.
	 */
	private ChannelHandlerContext outerContext;

	/**
	 * The context of the bottommost handler, i.e. the handler that will get upstream events as the first handler in
	 * the chain.
	 */
	private FilterPipelineChannelHandlerContext bottomContext;

	/**
	 * The context of the topmost handler, i.e. the handler that will get downstream events as the first handler in the
	 * chain.
	 */
	private FilterPipelineChannelHandlerContext topContext;

	public FilterPipelineImpl() {

		this.bottomHandler = new BottomHandler();
		this.bottomContext = new FilterPipelineChannelHandlerContext("bottomHandler", bottomHandler);
		this.topHandler = new TopHandler();
		this.topContext = new FilterPipelineChannelHandlerContext("topHandler", topHandler);

		this.bottomContext.setUpperContext(this.topContext);
		this.topContext.setLowerContext(this.bottomContext);

		this.channelSink = new ChannelSink();
		this.channel = new FilterPipelineChannel(this, this.channelSink);

		setChannelPipeline(null);
	}

	@Override
	public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
		this.outerContext = ctx;
	}

	@Override
	public void afterAdd(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}

	@Override
	public void beforeRemove(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}

	@Override
	public void afterRemove(final ChannelHandlerContext ctx) throws Exception {
		this.outerContext = ctx;
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		topHandler.handleDownstream(ctx, e);
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		bottomHandler.handleUpstream(ctx, e);
	}

	@Override
	public void setChannelPipeline(@Nullable final List<Tuple<String, ChannelHandler>> newChannelPipeline) {

		final Tuple<FilterPipelineChannelHandlerContext, FilterPipelineChannelHandlerContext> newContexts =
				createContextChain(newChannelPipeline);

		FilterPipelineChannelHandlerContext currentContext = topContext;

		while ((currentContext = currentContext.getLowerContext()) != bottomContext) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).beforeRemove(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}

		currentContext = newContexts.getSecond();
		while ((currentContext = currentContext.getLowerContext()) != newContexts.getFirst()) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).beforeAdd(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}

		final FilterPipelineChannelHandlerContext oldTopContext = topContext;
		final FilterPipelineChannelHandlerContext oldBottomContext = bottomContext;

		bottomContext = newContexts.getFirst();
		topContext = newContexts.getSecond();

		currentContext = bottomContext;
		while ((currentContext = currentContext.getUpperContext()) != topContext) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).afterAdd(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}

		currentContext = oldBottomContext;
		while ((currentContext = currentContext.getUpperContext()) != oldTopContext) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).afterRemove(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}
		}
	}

	private Tuple<FilterPipelineChannelHandlerContext, FilterPipelineChannelHandlerContext> createContextChain(
			final List<Tuple<String, ChannelHandler>> newChannelPipeline) {

		FilterPipelineChannelHandlerContext bottomContext = new FilterPipelineChannelHandlerContext(
				"bottomHandler", bottomHandler
		);

		FilterPipelineChannelHandlerContext topContext = new FilterPipelineChannelHandlerContext(
				"topHandler", topHandler
		);

		FilterPipelineChannelHandlerContext lowerContext = bottomContext;
		FilterPipelineChannelHandlerContext currentContext = null;

		if (newChannelPipeline != null) {

			for (Tuple<String, ChannelHandler> handlerTuple : newChannelPipeline) {

				// create context for the current handler
				currentContext = new FilterPipelineImpl.FilterPipelineChannelHandlerContext(
						handlerTuple.getFirst(),
						handlerTuple.getSecond()
				);

				// wire it together with the context below
				lowerContext.setUpperContext(currentContext);
				currentContext.setLowerContext(lowerContext);

				// remember current context as the one below for the next iteration
				lowerContext = currentContext;
			}
		}

		// if no context at all was created connect bottomContext to topContext directly
		if (currentContext == null) {
			bottomContext.setUpperContext(topContext);
			topContext.setLowerContext(bottomContext);
		}

		// otherwise connect the last (=topmost) context to the topContext
		else {
			currentContext.setUpperContext(topContext);
			topContext.setLowerContext(currentContext);
		}

		return new Tuple<FilterPipelineChannelHandlerContext, FilterPipelineChannelHandlerContext>(
				bottomContext,
				topContext
		);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> getChannelPipeline() {

		final List<Tuple<String, ChannelHandler>> list = newArrayList();

		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		while ((currentContext = currentContext.getUpperContext()) != topContext) {
			list.add(new Tuple<String, ChannelHandler>(currentContext.getName(), currentContext.getHandler()));
		}

		return list;
	}

	@Override
	public void addListener(final FilterPipelineDownstreamListener listener) {
		downstreamListenerManager.addListener(listener);
	}

	@Override
	public void addListener(final FilterPipelineUpstreamListener listener) {
		upstreamListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(final FilterPipelineDownstreamListener listener) {
		downstreamListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(final FilterPipelineUpstreamListener listener) {
		upstreamListenerManager.addListener(listener);
	}

	@Override
	public void addFirst(final String name, final ChannelHandler handler) {

		final FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(name, handler);
		final FilterPipelineChannelHandlerContext lowerContext = bottomContext;
		final FilterPipelineChannelHandlerContext upperContext = lowerContext.getUpperContext();

		setContextInBetween(newContext, lowerContext, upperContext);
	}

	private void setContextInBetween(final FilterPipelineImpl.FilterPipelineChannelHandlerContext newContext,
									 final FilterPipelineImpl.FilterPipelineChannelHandlerContext lowerContext,
									 final FilterPipelineImpl.FilterPipelineChannelHandlerContext upperContext) {

		newContext.setLowerContext(lowerContext);
		upperContext.setLowerContext(newContext);

		lowerContext.setUpperContext(newContext);
		newContext.setUpperContext(upperContext);
	}

	@Override
	public void addLast(final String name, final ChannelHandler handler) {

		final FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(name, handler);
		final FilterPipelineChannelHandlerContext upperContext = topContext;
		final FilterPipelineChannelHandlerContext lowerContext = upperContext.getLowerContext();

		setContextInBetween(newContext, lowerContext, upperContext);
	}

	@Override
	public void addBefore(final String baseName, final String name, final ChannelHandler handler) {

		final FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(name, handler);

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getName().equals(baseName)) {

				final FilterPipelineChannelHandlerContext upperContext = currentContext;
				final FilterPipelineChannelHandlerContext lowerContext = currentContext.getLowerContext();

				setContextInBetween(newContext, lowerContext, upperContext);
			}
		}
	}

	@Override
	public void addAfter(final String baseName, final String name, final ChannelHandler handler) {

		final FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(name, handler);

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = topContext.getLowerContext()) != bottomContext) {

			if (currentContext.getName().equals(baseName)) {

				FilterPipelineChannelHandlerContext lowerContext = currentContext;
				FilterPipelineChannelHandlerContext upperContext = currentContext.getUpperContext();

				setContextInBetween(newContext, lowerContext, upperContext);
			}
		}

	}

	@Override
	public void remove(final ChannelHandler handler) {

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getHandler() == handler) {
				removeContext(currentContext);
			}
		}

	}

	private void removeContext(final FilterPipelineChannelHandlerContext currentContext) {
		currentContext.getLowerContext().setUpperContext(currentContext.getUpperContext());
		currentContext.getUpperContext().setLowerContext(currentContext.getLowerContext());
	}

	@Override
	public ChannelHandler remove(final String name) {

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getName().equals(name)) {
				removeContext(currentContext);
				return currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public <T extends ChannelHandler> T remove(final Class<T> handlerType) {

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getHandler().getClass().equals(handlerType)) {
				removeContext(currentContext);
				return (T) currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public ChannelHandler removeFirst() {

		FilterPipelineChannelHandlerContext firstContextOverBottom = bottomContext.getUpperContext();

		if (firstContextOverBottom != topContext) {
			removeContext(firstContextOverBottom);
			return firstContextOverBottom.getHandler();
		}

		return null;
	}

	@Override
	public ChannelHandler removeLast() {

		FilterPipelineChannelHandlerContext firstContextUnderTop = topContext.getLowerContext();

		if (firstContextUnderTop != bottomContext) {
			removeContext(firstContextUnderTop);
			return firstContextUnderTop.getHandler();
		}

		return null;
	}

	@Override
	public void replace(final ChannelHandler oldHandler, final String newName, final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(newName, newHandler);

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getHandler() == oldHandler) {
				replaceContext(currentContext, newContext);
			}
		}
	}

	private void replaceContext(final FilterPipelineChannelHandlerContext oldContext,
								final FilterPipelineChannelHandlerContext newContext) {

		oldContext.getLowerContext().setUpperContext(newContext);
		newContext.setUpperContext(oldContext.getUpperContext());

		oldContext.getUpperContext().setLowerContext(newContext);
		newContext.setLowerContext(oldContext.getLowerContext());
	}

	@Override
	public ChannelHandler replace(final String oldName, final String newName, final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(newName, newHandler);

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getName().equals(oldName)) {
				replaceContext(currentContext, newContext);
				return currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public <T extends ChannelHandler> T replace(final Class<T> oldHandlerType, final String newName,
												final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext newContext = new FilterPipelineChannelHandlerContext(newName, newHandler);

		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {

			if (currentContext.getHandler().getClass().equals(oldHandlerType)) {
				replaceContext(currentContext, newContext);
				return (T) currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public ChannelHandler getFirst() {
		return bottomContext.getUpperContext() == topContext ? null : bottomContext.getUpperContext().getHandler();
	}

	@Override
	public ChannelHandler getLast() {
		return topContext.getLowerContext() == bottomContext ? null : topContext.getLowerContext().getHandler();
	}

	@Override
	public ChannelHandler get(final String name) {

		FilterPipelineChannelHandlerContext currentContext;

		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			if (currentContext.getName().equals(name)) {
				return currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public <T extends ChannelHandler> T get(final Class<T> handlerType) {

		FilterPipelineChannelHandlerContext currentContext;

		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			if (currentContext.getHandler().getClass().equals(handlerType)) {
				return (T) currentContext.getHandler();
			}
		}

		return null;
	}

	@Override
	public ChannelHandlerContext getContext(final ChannelHandler handler) {

		FilterPipelineChannelHandlerContext currentContext;

		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			if (currentContext.getHandler() == handler) {
				return currentContext;
			}
		}

		return null;
	}

	@Override
	public ChannelHandlerContext getContext(final String name) {

		FilterPipelineChannelHandlerContext currentContext;

		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			if (currentContext.getName().equals(name)) {
				return currentContext;
			}
		}

		return null;
	}

	@Override
	public ChannelHandlerContext getContext(final Class<? extends ChannelHandler> handlerType) {

		FilterPipelineChannelHandlerContext currentContext;

		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			if (currentContext.getHandler().getClass().equals(handlerType)) {
				return currentContext;
			}
		}

		return null;
	}

	@Override
	public void sendUpstream(final ChannelEvent e) {
		bottomContext.sendUpstream(e);
	}

	@Override
	public void sendDownstream(final ChannelEvent e) {
		topContext.sendDownstream(e);
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public org.jboss.netty.channel.ChannelSink getSink() {
		return channelSink;
	}

	@Override
	public void attach(final Channel channel, final org.jboss.netty.channel.ChannelSink sink) {
		this.channel = channel;
		this.channelSink = sink;
	}

	@Override
	public boolean isAttached() {
		return channel != null && channelSink != null;
	}

	@Override
	public List<String> getNames() {
		if (!isAttached()) {
			return null;
		}
		List<String> names = newArrayList();
		FilterPipelineChannelHandlerContext currentContext;
		while ((currentContext = bottomContext.getUpperContext()) != topContext) {
			names.add(currentContext.getName());
		}
		return names;
	}

	@Override
	public Map<String, ChannelHandler> toMap() {
		throw new RuntimeException("TODO implement");
	}
}
