package com.coalesenses.isense.ishell.interpreter;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacket;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketType;

public class IShellInterpreterHandler extends SimpleChannelHandler {
    private final org.slf4j.Logger log;

    private Channel channel;

    /**
     * Package-private constructor for creation via factory only
     */
    IShellInterpreterHandler() {
        this(null);
    }

    /**
     * Package-private constructor for creation via factory only
     */
    IShellInterpreterHandler(String instanceName) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : IShellInterpreterHandler.class.getName());
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        channel = null;
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        super.channelConnected(ctx, e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (e.getMessage() instanceof IShellInterpreterSetChannelMessage) {
            IShellInterpreterSetChannelMessage msg = (IShellInterpreterSetChannelMessage) e.getMessage();
            setChannel(msg.getChannel());
        } else {
            super.writeRequested(ctx, e);
        }

    }

    public void setChannel(byte channelNumber) {
        if (this.channel == null) {
            throw new RuntimeException("Channel not yet connected");
        }

        ChannelBuffer buffer = ChannelBuffers.buffer(3);
        buffer.writeByte(ISensePacketType.CODE.getValue());
        buffer.writeByte(IShellInterpreterPacketTypes.COMMAND_SET_CHANNEL.getValue());
        buffer.writeByte(channelNumber);

        log.debug("Setting channel to {}", channelNumber);
        channel.write(new ISensePacket(buffer));
    }

}
