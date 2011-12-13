package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A FilterPipeline is an embeddable Pipeline of JBoss Netty compatible ChannelHandlers. The set of handlers in the
 * pipeline can be reconfigured at runtime.
 */
public interface FilterPipeline extends ChannelDownstreamHandler, ChannelUpstreamHandler, LifeCycleAwareChannelHandler,
		ChannelPipeline {

	/**
	 * Sets or replaces the current handler pipeline (if existing) with the given list of handlers. The head of the
	 * list will be on the bottom of the resulting pipeline, the tail on top.
	 *
	 * @param channelPipeline
	 * 		the handlers to set into the pipeline or {@code null} if the resulting pipeline should
	 * 		be empty (pass-through mode). The String argument of the tuple is a handler name, the ChannelHandler
	 * 		argument is the actual handler to set.
	 */
	void setChannelPipeline(@Nullable List<Tuple<String, ChannelHandler>> channelPipeline);

	/**
	 * Returns the current handler pipeline.
	 *
	 * @return the current handler pipeline
	 */
	@Nonnull List<Tuple<String, ChannelHandler>> getChannelPipeline();

}
