package de.uniluebeck.itm.nettyprotocols.util;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public abstract class OneToOneDownstreamDecoder extends OneToOneEncoder {

	protected abstract Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception;

	@Override
	protected final Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		return decode(ctx, channel, msg);
	}

}
