package de.uniluebeck.itm.netty.handlerstack;

import org.jboss.netty.channel.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

class FilterPipelineChannelHandlerContext implements ChannelHandlerContext {

	private volatile FilterPipelineChannelHandlerContext upperContext;

	private volatile FilterPipelineChannelHandlerContext lowerContext;

	private final String name;

	private final ChannelHandler handler;

	private final boolean canHandleUpstream;

	private final boolean canHandleDownstream;

	private final FilterPipelineImpl pipeline;

	private volatile Object attachment;

	FilterPipelineChannelHandlerContext(@Nonnull final FilterPipelineImpl pipeline, @Nonnull final String name,
										@Nonnull final ChannelHandler handler) {

		checkNotNull(pipeline);
		checkNotNull(name);
		checkNotNull(handler);

		this.canHandleUpstream = handler instanceof ChannelUpstreamHandler;
		this.canHandleDownstream = handler instanceof ChannelDownstreamHandler;

		if (!canHandleUpstream && !canHandleDownstream) {
			throw new IllegalArgumentException(
					"handler must be either " +
							ChannelUpstreamHandler.class.getName() + " or " +
							ChannelDownstreamHandler.class.getName() + '.'
			);
		}

		this.pipeline = pipeline;
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
		return pipeline;
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

		FilterPipelineChannelHandlerContext nextDownstreamHandlerContext = getNextDownstreamHandlerContext();

		if (nextDownstreamHandlerContext == null) {

			if (pipeline.isUsedAsHandler) {

				ChannelHandlerContext outerContext = pipeline.getOuterContext();
				outerContext.sendDownstream(e);

			} else if (pipeline.isAttached) {

				try {
					//noinspection ConstantConditions
					pipeline.getSink().eventSunk(pipeline, e);
				} catch (Exception e1) {
					sendUpstream(new DefaultExceptionEvent(pipeline.getChannel(), e1));
				}

			}

		} else {

			ChannelDownstreamHandler nextDownstreamHandler =
					(ChannelDownstreamHandler) nextDownstreamHandlerContext.getHandler();

			try {

				nextDownstreamHandler.handleDownstream(nextDownstreamHandlerContext, e);

			} catch (Exception e1) {

				try {

					DefaultExceptionEvent exceptionEvent = new DefaultExceptionEvent(nextDownstreamHandlerContext.getChannel(), e1);
					nextDownstreamHandler.handleDownstream(nextDownstreamHandlerContext, exceptionEvent);

				} catch (Exception e2) {
					throw propagate(e2);
				}
			}
		}
	}

	@Override
	public void sendUpstream(ChannelEvent e) {

		FilterPipelineChannelHandlerContext nextUpstreamHandlerContext = getNextUpstreamHandlerContext();

		if (nextUpstreamHandlerContext == null) {

			if (pipeline.getOuterContext() != null) {

				ChannelHandlerContext outerContext = pipeline.getOuterContext();
				outerContext.sendUpstream(e);
			}

		} else {

			ChannelUpstreamHandler nextUpstreamHandler =
					(ChannelUpstreamHandler) nextUpstreamHandlerContext.getHandler();

			try {

				nextUpstreamHandler.handleUpstream(nextUpstreamHandlerContext, e);

			} catch (Exception e1) {

				try {

					DefaultExceptionEvent exceptionEvent = new DefaultExceptionEvent(nextUpstreamHandlerContext.getChannel(), e1);
					nextUpstreamHandler.handleUpstream(nextUpstreamHandlerContext, exceptionEvent);

				} catch (Exception e2) {
					throw propagate(e2);
				}
			}
		}
	}

	@Nullable
	private FilterPipelineChannelHandlerContext getNextDownstreamHandlerContext() {
		FilterPipelineChannelHandlerContext currentContext = lowerContext;
		while (currentContext != null) {
			if (currentContext.canHandleDownstream()) {
				return currentContext;
			}
			currentContext = currentContext.getLowerContext();
		}
		return null;
	}

	@Nullable
	private FilterPipelineChannelHandlerContext getNextUpstreamHandlerContext() {
		FilterPipelineChannelHandlerContext currentContext = upperContext;
		while (currentContext != null) {
			if (currentContext.canHandleUpstream()) {
				return currentContext;
			}
			currentContext = currentContext.getUpperContext();
		}
		return null;
	}

}