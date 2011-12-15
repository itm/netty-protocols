package de.uniluebeck.itm.netty.handlerstack;

import com.google.common.base.Throwables;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

public class FilterPipelineImpl implements FilterPipeline {

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineImpl.class);

	private static final String ERROR_MESSAGE_NOT_ATTACHED_AND_NOT_HANDLER =
			"This FilterPipeline instance is neither used as pipeline nor was it added to another pipeline as a handler!";

	private static final String ERROR_MESSAGE_ALREADY_ADDED_AS_HANDLER =
			"A FilterPipeline instance may only be added once to another pipeline!";

	private static final String ERROR_MESSAGE_ALREADY_ATTACHED_AS_PIPELINE =
			"A FilterPipeline instance may only be used once as handler OR pipeline!";

	private org.jboss.netty.channel.ChannelSink outerChannelSink;

	private Channel outerChannel;

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

	private volatile boolean isUsedAsHandler = false;

	private volatile boolean isAttached = false;

	@Override
	public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {

		checkState(!isUsedAsHandler, ERROR_MESSAGE_ALREADY_ADDED_AS_HANDLER);
		checkState(!isAttached, ERROR_MESSAGE_ALREADY_ATTACHED_AS_PIPELINE);

	}

	@Override
	public void afterAdd(final ChannelHandlerContext ctx) throws Exception {

		checkState(!isUsedAsHandler, ERROR_MESSAGE_ALREADY_ADDED_AS_HANDLER);
		checkState(!isAttached, ERROR_MESSAGE_ALREADY_ATTACHED_AS_PIPELINE);

		outerContext = ctx;
		outerChannel = ctx.getChannel();
		outerChannelSink = ctx.getChannel() != null ? ctx.getChannel().getPipeline().getSink() : null;
		isUsedAsHandler = true;
	}

	@Override
	public void beforeRemove(final ChannelHandlerContext ctx) throws Exception {

		checkState(isUsedAsHandler);
		checkState(!isAttached);

	}

	@Override
	public void afterRemove(final ChannelHandlerContext ctx) throws Exception {

		checkState(isUsedAsHandler);
		checkState(!isAttached);

		outerContext = null;
		outerChannel = null;
		outerChannelSink = null;
		isUsedAsHandler = false;
	}

	@Override
	public void handleDownstream(@Nonnull final ChannelHandlerContext ctx, @Nonnull final ChannelEvent e)
			throws Exception {

		checkNotNull(ctx);
		checkNotNull(e);
		checkState(isAttached || isUsedAsHandler, ERROR_MESSAGE_NOT_ATTACHED_AND_NOT_HANDLER);

		if (e instanceof UpstreamMessageEvent || e instanceof UpstreamChannelStateEvent) {
			throw new IllegalArgumentException("handleDownstream does not handle upstream events!");
		}

		FilterPipelineChannelHandlerContext highestDownstreamHandlerContext = getHighestDownstreamHandlerContext();
		if (highestDownstreamHandlerContext != null) {
			ChannelDownstreamHandler downstreamHandler =
					(ChannelDownstreamHandler) highestDownstreamHandlerContext.getHandler();
			downstreamHandler.handleDownstream(highestDownstreamHandlerContext, e);
		} else if (outerContext != null) {
			outerContext.sendDownstream(e);
		}
	}

	@Override
	public void handleUpstream(@Nonnull final ChannelHandlerContext ctx, @Nonnull final ChannelEvent e)
			throws Exception {

		checkNotNull(ctx);
		checkNotNull(e);
		checkState(isAttached || isUsedAsHandler, ERROR_MESSAGE_NOT_ATTACHED_AND_NOT_HANDLER);

		if (e instanceof DownstreamMessageEvent || e instanceof DownstreamChannelStateEvent) {
			throw new IllegalArgumentException("handleUpstream does not handle downstream events!");
		}

		FilterPipelineChannelHandlerContext lowestUpstreamHandlerContext = getLowestUpstreamHandlerContext();
		if (lowestUpstreamHandlerContext != null) {
			ChannelUpstreamHandler upstreamHandler = (ChannelUpstreamHandler) lowestUpstreamHandlerContext.getHandler();
			upstreamHandler.handleUpstream(lowestUpstreamHandlerContext, e);
		} else if (outerContext != null) {
			outerContext.sendUpstream(e);
		}
	}

	@Override
	public void setChannelPipeline(@Nullable final List<Tuple<String, ChannelHandler>> newChannelPipeline) {

		FilterPipelineChannelHandlerContext newBottomContext = null;
		FilterPipelineChannelHandlerContext newTopContext = null;

		if (newChannelPipeline != null) {

			FilterPipelineChannelHandlerContext currentContext = null;

			for (Tuple<String, ChannelHandler> tuple : newChannelPipeline) {

				FilterPipelineChannelHandlerContext newContext =
						new FilterPipelineChannelHandlerContext(this, tuple.getFirst(), tuple.getSecond());
				newContext.setLowerContext(currentContext);

				if (currentContext == null) {
					newBottomContext = newContext;
				} else {
					currentContext.setUpperContext(newContext);
				}

				currentContext = newContext;
			}

			newTopContext = currentContext;
		}

		notifyBeforeRemove(bottomContext);
		notifyBeforeAdd(newBottomContext);

		final FilterPipelineChannelHandlerContext oldBottomContext = bottomContext;
		bottomContext = newBottomContext;
		topContext = newTopContext;

		notifyAfterAdd(bottomContext);
		notifyAfterRemove(oldBottomContext);
	}

	private void notifyAfterRemove(@Nullable FilterPipelineChannelHandlerContext currentContext) {

		while (currentContext != null) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).afterRemove(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}

			currentContext = currentContext.getUpperContext();
		}
	}

	private void notifyAfterAdd(@Nullable FilterPipelineChannelHandlerContext currentContext) {

		while (currentContext != null) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).afterAdd(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}

			currentContext = currentContext.getUpperContext();
		}
	}

	private void notifyBeforeAdd(@Nullable FilterPipelineChannelHandlerContext currentContext) {
		while (currentContext != null) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).beforeAdd(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}

			currentContext = currentContext.getUpperContext();
		}
	}

	private void notifyBeforeRemove(@Nullable FilterPipelineChannelHandlerContext currentContext) {
		while (currentContext != null) {

			final ChannelHandler handler = currentContext.getHandler();

			if (handler instanceof LifeCycleAwareChannelHandler) {
				try {
					((LifeCycleAwareChannelHandler) handler).beforeRemove(currentContext);
				} catch (Exception e) {
					log.warn("" + e, e);
				}
			}

			currentContext = currentContext.getUpperContext();
		}
	}

	@Nonnull
	@Override
	public List<Tuple<String, ChannelHandler>> getChannelPipeline() {

		checkPipelineState();

		final List<Tuple<String, ChannelHandler>> list = newArrayList();

		if (bottomContext == null) {
			return list;
		}

		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		list.add(new Tuple<String, ChannelHandler>(currentContext.getName(), currentContext.getHandler()));

		while ((currentContext = currentContext.getUpperContext()) != null) {
			list.add(new Tuple<String, ChannelHandler>(currentContext.getName(), currentContext.getHandler()));
		}

		return list;
	}

	private void checkPipelineState() {
		checkState((bottomContext == null && topContext == null) || (bottomContext != null && topContext != null));
	}

	@Override
	public void addAfter(@Nonnull final String baseName, @Nonnull final String name,
						 @Nonnull final ChannelHandler handler) {

		checkNotNull(baseName);
		checkNotNull(name);
		checkNotNull(handler);

		checkPipelineState();

		final FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, name, handler);

		FilterPipelineChannelHandlerContext baseContext = getContextOrDie(baseName);
		insertContextInBetween(newContext, baseContext, baseContext.getUpperContext());

		checkPipelineState();
	}

	@Override
	public void addBefore(@Nonnull final String baseName, @Nonnull final String name,
						  @Nonnull final ChannelHandler handler) {

		checkNotNull(baseName);
		checkNotNull(name);
		checkNotNull(handler);

		checkPipelineState();

		final FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, name, handler);

		FilterPipelineChannelHandlerContext baseContext = getContextOrDie(baseName);
		insertContextInBetween(newContext, baseContext.getLowerContext(), baseContext);

		checkPipelineState();
	}

	@Override
	public void addFirst(@Nonnull final String name, @Nonnull final ChannelHandler handler) {

		checkNotNull(name);
		checkNotNull(handler);

		checkPipelineState();

		final FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, name, handler);

		insertContextInBetween(newContext, null, bottomContext);

		checkPipelineState();
	}

	@Override
	public void addLast(@Nonnull final String name, @Nonnull final ChannelHandler handler) {

		checkNotNull(name);
		checkNotNull(handler);

		checkPipelineState();

		final FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, name, handler);

		insertContextInBetween(newContext, topContext, null);

		checkPipelineState();
	}

	@Override
	public void attach(@Nonnull final Channel outerChannel,
					   @Nonnull final org.jboss.netty.channel.ChannelSink outerChannelSink) {

		checkNotNull(outerChannel);
		checkNotNull(outerChannelSink);

		checkState(!isUsedAsHandler, "A FilterPipeline instance may only be used once as handler OR pipeline!");
		checkState(!isAttached, "A FilterPipeline instance may only be attached once!");

		this.outerChannel = outerChannel;
		this.outerChannelSink = outerChannelSink;
		this.isAttached = true;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ChannelHandler> T get(@Nonnull final Class<T> handlerType) {

		checkNotNull(handlerType);

		FilterPipelineChannelHandlerContext currentContext = bottomContext;

		while (currentContext != null) {

			if (currentContext.getHandler().getClass().equals(handlerType)) {
				return (T) currentContext.getHandler();
			}

			currentContext = currentContext.getUpperContext();
		}

		return null;
	}

	@Override
	@Nullable
	public ChannelHandler get(@Nonnull final String name) {

		checkNotNull(name);

		ChannelHandlerContext context = getContext(name);
		return context == null ?
				null :
				context.getHandler();
	}

	@Override
	@Nullable
	public Channel getChannel() {
		return outerChannel;
	}

	@Override
	@Nullable
	public ChannelHandlerContext getContext(@Nonnull final ChannelHandler handler) {

		checkNotNull(handler);

		FilterPipelineChannelHandlerContext currentContext = bottomContext;

		while (currentContext != null) {

			if (currentContext.getHandler() == handler) {
				return currentContext;
			}

			currentContext = currentContext.getUpperContext();
		}

		return null;
	}

	@Override
	@Nullable
	public ChannelHandlerContext getContext(@Nonnull final Class<? extends ChannelHandler> handlerType) {

		checkNotNull(handlerType);

		FilterPipelineChannelHandlerContext currentContext = bottomContext;

		while (currentContext != null) {

			if (currentContext.getHandler().getClass().equals(handlerType)) {
				return currentContext;
			}

			currentContext = currentContext.getUpperContext();
		}

		return null;
	}

	@Override
	@Nullable
	public ChannelHandlerContext getContext(@Nonnull final String name) {

		checkNotNull(name);

		FilterPipelineChannelHandlerContext currentContext = bottomContext;

		while (currentContext != null) {

			if (currentContext.getName().equals(name)) {
				return currentContext;
			}

			currentContext = currentContext.getUpperContext();
		}

		return null;
	}

	@Nonnull
	private FilterPipelineChannelHandlerContext getContextOrDie(@Nonnull final String name) {

		checkNotNull(name);

		ChannelHandlerContext context = getContext(name);
		if (context == null) {
			throw new NoSuchElementException(name);
		}
		return (FilterPipelineChannelHandlerContext) context;
	}

	@Override
	@Nullable
	public ChannelHandler getFirst() {
		return bottomContext != null ? bottomContext.getHandler() : null;
	}

	@Override
	@Nullable
	public ChannelHandler getLast() {
		return topContext != null ? topContext.getHandler() : null;
	}

	@Override
	@Nonnull
	public List<String> getNames() {

		List<String> names = newArrayList();

		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		while (currentContext != null) {
			names.add(currentContext.getName());
			currentContext = currentContext.getUpperContext();
		}

		return names;
	}

	@Override
	@Nullable
	public org.jboss.netty.channel.ChannelSink getSink() {
		return outerChannelSink;
	}

	@Override
	public boolean isAttached() {
		return isAttached;
	}

	@Override
	public void remove(final ChannelHandler handler) {
		removeContext(getContextOrDie(handler));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends ChannelHandler> T remove(final Class<T> handlerType) {

		checkPipelineState();

		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		while (bottomContext != null) {

			if (currentContext.getHandler().getClass().equals(handlerType)) {
				removeContext(currentContext);
				checkPipelineState();
				return (T) currentContext.getHandler();
			}

			currentContext = currentContext.getUpperContext();
		}

		return null;
	}

	@Nonnull
	private FilterPipelineChannelHandlerContext getContextOrDie(@Nonnull final ChannelHandler handler) {
		checkNotNull(handler);
		ChannelHandlerContext context = getContext(handler);
		if (context == null) {
			throw new NoSuchElementException("No such handler: " + handler);
		}
		return (FilterPipelineChannelHandlerContext) context;
	}

	@Nonnull
	private <T extends ChannelHandler> FilterPipelineChannelHandlerContext getContextOrDie(
			@Nonnull final Class<T> handlerType) {
		checkNotNull(handlerType);
		ChannelHandlerContext context = getContext(handlerType);
		if (context == null) {
			throw new NoSuchElementException("No such handler: " + handlerType);
		}
		return (FilterPipelineChannelHandlerContext) context;
	}

	private boolean isEmpty() {
		return bottomContext == null && topContext == null;
	}

	@Override
	@Nonnull
	public ChannelHandler remove(@Nonnull final String name) {
		checkNotNull(name);
		FilterPipelineChannelHandlerContext contextToBeRemoved = getContextOrDie(name);
		removeContext(contextToBeRemoved);
		return contextToBeRemoved.getHandler();
	}

	@Override
	@Nonnull
	public ChannelHandler removeFirst() {

		checkPipelineState();

		ChannelHandler first = getFirst();
		if (first == null) {
			throw new NoSuchElementException();
		}
		remove(first);
		return first;
	}

	@Override
	@Nonnull
	public ChannelHandler removeLast() {

		checkPipelineState();

		ChannelHandler last = getLast();
		if (last == null) {
			throw new NoSuchElementException();
		}
		remove(last);
		return last;
	}

	@Override
	public void replace(final ChannelHandler oldHandler, final String newName, final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext oldContext = getContextOrDie(oldHandler);
		FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, newName, newHandler);

		replaceContext(oldContext, newContext);
	}

	@Override
	public ChannelHandler replace(final String oldName, final String newName, final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext oldContext = getContextOrDie(oldName);
		FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, newName, newHandler);

		replaceContext(oldContext, newContext);

		return oldContext.getHandler();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends ChannelHandler> T replace(final Class<T> oldHandlerType, final String newName,
												final ChannelHandler newHandler) {

		FilterPipelineChannelHandlerContext oldContext = getContextOrDie(oldHandlerType);
		FilterPipelineChannelHandlerContext newContext =
				new FilterPipelineChannelHandlerContext(this, newName, newHandler);

		replaceContext(oldContext, newContext);

		return (T) oldContext.getHandler();
	}

	@Override
	public void sendDownstream(final ChannelEvent e) {

		checkState(isAttached || isUsedAsHandler);

		FilterPipelineChannelHandlerContext highestDownstreamHandlerContext = getHighestDownstreamHandlerContext();

		if (highestDownstreamHandlerContext != null) {

			try {

				ChannelDownstreamHandler downstreamHandler =
						(ChannelDownstreamHandler) highestDownstreamHandlerContext.getHandler();
				downstreamHandler.handleDownstream(highestDownstreamHandlerContext, e);

			} catch (Exception e1) {
				notifyHandlerException(e, e1);
			}
		}
	}

	@Override
	public void sendUpstream(final ChannelEvent e) {

		checkState(isAttached || isUsedAsHandler);

		FilterPipelineChannelHandlerContext lowestUpstreamHandlerContext = getLowestUpstreamHandlerContext();

		if (lowestUpstreamHandlerContext != null) {

			try {

				ChannelUpstreamHandler upstreamHandler =
						(ChannelUpstreamHandler) lowestUpstreamHandlerContext.getHandler();
				upstreamHandler.handleUpstream(lowestUpstreamHandlerContext, e);

			} catch (Exception e1) {
				notifyHandlerException(e, e1);
			}
		}
	}

	private void notifyHandlerException(final ChannelEvent e, final Exception e1) {
		if (e instanceof ExceptionEvent) {
			log.warn(
					"An exception was thrown by a user handler while handling an exception event: {}\nCaused by: {}",
					Throwables.getStackTraceAsString(e1), Throwables.getStackTraceAsString(((ExceptionEvent) e).getCause())
			);
			return;
		}

		ChannelPipelineException channelPipelineException;
		if (e1 instanceof ChannelPipelineException) {
			channelPipelineException = (ChannelPipelineException) e1;
		} else {
			channelPipelineException = new ChannelPipelineException(e1);
		}

		try {
			outerChannelSink.exceptionCaught(this, e, channelPipelineException);
		} catch (Exception e2) {
			log.warn("Sink threw an exception while handling an exception: {}", channelPipelineException);
		}
	}

	@Nullable
	private FilterPipelineChannelHandlerContext getLowestUpstreamHandlerContext() {
		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		while (currentContext != null) {
			if (currentContext.canHandleUpstream()) {
				return currentContext;
			}
			currentContext = currentContext.getUpperContext();
		}
		return null;
	}

	ChannelHandlerContext getOuterContext() {
		return outerContext;
	}

	@Nullable
	private FilterPipelineChannelHandlerContext getHighestDownstreamHandlerContext() {
		FilterPipelineChannelHandlerContext currentContext = topContext;
		while (currentContext != null) {
			if (currentContext.canHandleDownstream()) {
				return currentContext;
			}
			currentContext = currentContext.getLowerContext();
		}
		return null;
	}

	@Override
	@Nonnull
	public Map<String, ChannelHandler> toMap() {

		Map<String, ChannelHandler> map = new LinkedHashMap<String, ChannelHandler>();

		FilterPipelineChannelHandlerContext currentContext = bottomContext;
		while (currentContext != null) {
			map.put(currentContext.getName(), currentContext.getHandler());
			currentContext = currentContext.getUpperContext();
		}

		return map;
	}

	private void removeContext(@Nonnull final FilterPipelineChannelHandlerContext contextToBeRemoved) {

		checkNotNull(contextToBeRemoved);

		if (isEmpty()) {
			return;
		}

		if (contextToBeRemoved.getUpperContext() == null) {
			topContext = contextToBeRemoved.getLowerContext();
		} else {
			contextToBeRemoved.getUpperContext().setLowerContext(contextToBeRemoved.getLowerContext());
		}

		if (contextToBeRemoved.getLowerContext() == null) {
			bottomContext = contextToBeRemoved.getUpperContext();
		} else {
			contextToBeRemoved.getLowerContext().setUpperContext(contextToBeRemoved.getUpperContext());
		}
	}

	private void replaceContext(@Nonnull final FilterPipelineChannelHandlerContext oldContext,
								@Nonnull final FilterPipelineChannelHandlerContext newContext) {

		checkNotNull(oldContext);
		checkNotNull(newContext);

		removeContext(oldContext);
		insertContextInBetween(newContext, oldContext.getLowerContext(), oldContext.getUpperContext());
	}

	private void insertContextInBetween(@Nonnull final FilterPipelineChannelHandlerContext newContext,
										@Nullable final FilterPipelineChannelHandlerContext lowerContext,
										@Nullable final FilterPipelineChannelHandlerContext upperContext) {

		checkNotNull(newContext);

		if (isEmpty() && (lowerContext != null || upperContext != null)) {
			throw new IllegalArgumentException();
		} else if (!isEmpty() && lowerContext == null && upperContext == null) {
			throw new IllegalArgumentException();
		}

		if (isEmpty()) {
			bottomContext = newContext;
			topContext = newContext;
			return;
		}

		newContext.setLowerContext(lowerContext);
		newContext.setUpperContext(upperContext);

		if (upperContext == null) {
			topContext = newContext;
		} else {
			upperContext.setLowerContext(newContext);
		}

		if (lowerContext == null) {
			bottomContext = newContext;
		} else {
			lowerContext.setUpperContext(newContext);
		}
	}
}
