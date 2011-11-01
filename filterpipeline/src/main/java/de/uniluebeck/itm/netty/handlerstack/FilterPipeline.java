package de.uniluebeck.itm.netty.handlerstack;

import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.UpstreamChannelStateEvent;

import java.net.SocketAddress;
import java.util.List;

public interface FilterPipeline {

    void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e);

    public interface DownstreamOutputListener {
        void receiveDownstreamOutput(ChannelBuffer message, SocketAddress targetAddress);

        void downstreamExceptionCaught(Throwable e);
    }

    public interface UpstreamOutputListener {
        void receiveUpstreamOutput(ChannelBuffer message, SocketAddress sourceAddress);

        void upstreamExceptionCaught(Throwable e);
    }

    void sendDownstream(ChannelBuffer message, SocketAddress targetAddress);

    void sendUpstream(ChannelBuffer message, SocketAddress sourceAddress);

    void sendUpstream(UpstreamChannelStateEvent message, SocketAddress sourceAddress);

    void setChannelPipeline(List<Tuple<String, ChannelHandler>> channelPipeline);

    List<Tuple<String, ChannelHandler>> getChannelPipeline();

    void addListener(final DownstreamOutputListener listener);

    void addListener(final UpstreamOutputListener listener);

    void removeListener(final DownstreamOutputListener listener);

    void removeListener(final UpstreamOutputListener listener);

}
