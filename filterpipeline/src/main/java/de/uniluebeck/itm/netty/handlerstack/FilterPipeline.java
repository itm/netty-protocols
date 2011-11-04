package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.*;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A FilterPipeline is an embeddable Pipeline of JBoss Netty compatible ChannelHandlers. The set of handlers in the
 * pipeline can be reconfigured at runtime. Listeners can attach to both the top of the pipeline for listening to
 * upstream messages and to the bottom of the pipeline to listen for downstream messages.
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
	List<Tuple<String, ChannelHandler>> getChannelPipeline();

	/**
	 * Adds a listener to the bottom of this pipeline that will be notified of downstream events.
	 *
	 * @param listener
	 * 		the listener to add
	 */
	void addListener(final FilterPipelineDownstreamListener listener);

	/**
	 * Adds a listener to the top of this pipeline that will be notified of upstream events.
	 *
	 * @param listener
	 * 		the listener to add
	 */
	void addListener(final FilterPipelineUpstreamListener listener);

	/**
	 * Removes a listener from the bottom of this pipeline.
	 *
	 * @param listener
	 * 		the listener to remove
	 */
	void removeListener(final FilterPipelineDownstreamListener listener);

	/**
	 * Removes a listener from the top of this pipeline.
	 *
	 * @param listener
	 * 		the listener to remove
	 */
	void removeListener(final FilterPipelineUpstreamListener listener);

}
