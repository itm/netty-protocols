package de.uniluebeck.itm.netty.handlerstack;

import org.jboss.netty.channel.ChannelPipeline;

public interface FilterPipelineEmbedder extends ChannelPipeline {

	void addListener(FilterPipelineDownstreamListener listener);

	void addListener(FilterPipelineUpstreamListener listener);

	void removeListener(FilterPipelineDownstreamListener listener);

	void removeListener(FilterPipelineUpstreamListener listener);

}
