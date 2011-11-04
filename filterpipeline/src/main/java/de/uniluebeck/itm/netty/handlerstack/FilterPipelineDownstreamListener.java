package de.uniluebeck.itm.netty.handlerstack;

import org.jboss.netty.channel.ChannelDownstreamHandler;

public interface FilterPipelineDownstreamListener extends ChannelDownstreamHandler {

	void downstreamExceptionCaught(Throwable e);

}