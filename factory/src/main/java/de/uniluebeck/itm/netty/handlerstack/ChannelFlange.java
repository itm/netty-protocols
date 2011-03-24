package de.uniluebeck.itm.netty.handlerstack;

import java.util.Random;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.google.common.base.Preconditions;

public class ChannelFlange {
    private static final Random r = new Random();

    class ProxyHandler extends SimpleChannelHandler {
        private long id = r.nextLong();
        private Channel channel = null;
        private ProxyHandler otherProxyHandler;

        public void registerWith(Channel channel) {
            Preconditions.checkNotNull(channel);

            ChannelPipeline pipeline = channel.getPipeline();

            if (!(pipeline.getLast() == this)) {
                if( pipeline.toMap().entrySet().contains(this))
                    pipeline.remove(this);
                
                pipeline.addLast("proxy-" + id, this);
            }
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            channel = e.getChannel();
            registerWith(channel);
            super.channelConnected(ctx, e);
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            channel = null;
            super.channelDisconnected(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            channel = e.getChannel();
            registerWith(channel);

            if (getOtherProxyHandler().getChannel() != null)
                getOtherProxyHandler().getChannel().write(e.getMessage());
            
            super.messageReceived(ctx, e);
        }

        public Channel getChannel() {
            return channel;
        }

        public void setOtherProxyHandler(ProxyHandler otherProxyHandler) {
            this.otherProxyHandler = otherProxyHandler;
        }

        public ProxyHandler getOtherProxyHandler() {
            return otherProxyHandler;
        }

    }

    private final ProxyHandler left = new ProxyHandler();
    private final ProxyHandler right = new ProxyHandler();

    public ChannelFlange() {
        left.setOtherProxyHandler(this.right);
        right.setOtherProxyHandler(this.left);
    }

    public void setLeftChannel(Channel leftChannel) {
        left.registerWith(leftChannel);
    }
    
    public void setRightChannel(Channel rightChannel) {
        right.registerWith(rightChannel);
    }
    
    public ChannelHandler getLeftHandler() {
        return left;
    }

    public ChannelHandler getRigthHandler() {
        return right;
    }

}
