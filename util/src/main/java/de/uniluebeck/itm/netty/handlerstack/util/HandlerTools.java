package de.uniluebeck.itm.netty.handlerstack.util;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.UpstreamMessageEvent;

public class HandlerTools {

    public static void sendDownstream(Object msg, ChannelHandlerContext context) {
        Channel channel = context.getChannel();

        DownstreamMessageEvent event =
                new DownstreamMessageEvent(channel, Channels.succeededFuture(channel), msg, channel.getRemoteAddress());

        context.sendDownstream(event);
    }

    public static void sendUpstream(Object msg, ChannelHandlerContext context) {
        Channel channel = context.getChannel();

        UpstreamMessageEvent event = new UpstreamMessageEvent(channel, msg, channel.getRemoteAddress());

        context.sendUpstream(event);
    }
}
