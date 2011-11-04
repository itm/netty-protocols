package de.uniluebeck.itm.netty.handlerstack;

import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface FilterPipelineUpstreamListener extends ChannelUpstreamHandler {

	void upstreamExceptionCaught(Throwable e);

}