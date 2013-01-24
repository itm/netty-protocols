package de.uniluebeck.itm.netty.util;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public abstract class OneToOneUpstreamEncoder extends OneToOneDecoder {

	@Override
	protected final Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		return encode(ctx, channel, msg);
	}

	protected abstract Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception;

}
