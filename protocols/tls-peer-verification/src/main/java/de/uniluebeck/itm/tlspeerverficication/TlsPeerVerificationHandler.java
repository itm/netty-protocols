package de.uniluebeck.itm.tlspeerverficication;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.ssl.SslHandler;

import com.google.common.base.Preconditions;

public class TlsPeerVerificationHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Get the SslHandler from the pipeline
        // which were added in SecureChatPipelineFactory.
        SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
        Preconditions.checkNotNull(sslHandler, "No SSL handler in the current pipeline");

        //Begin handshake
        sslHandler.handshake();
    }

}
