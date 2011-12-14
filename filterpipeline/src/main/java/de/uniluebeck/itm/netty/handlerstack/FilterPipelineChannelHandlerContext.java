package de.uniluebeck.itm.netty.handlerstack;

import org.jboss.netty.channel.*;

import javax.annotation.Nonnull;

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

		// if we are at the bottom of the stack
		if (lowerContext == null) {

			ChannelHandlerContext outerContext = pipeline.getOuterContext();
			outerContext.sendDownstream(e);

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
					throw propagate(e2);
				}
			}
		}
	}

	@Override
	public void sendUpstream(ChannelEvent e) {

		// if we are at the top of the stack
		if (upperContext == null) {

			ChannelHandlerContext outerContext = pipeline.getOuterContext();
			outerContext.sendUpstream(e);

		}

		// if there's at least one handler above us
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
					throw propagate(e2);
				}
			}
		}
	}

}