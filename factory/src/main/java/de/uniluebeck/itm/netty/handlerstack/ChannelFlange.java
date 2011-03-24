/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

/**
 * Used to interconnect two Netty channels via proxy handlers. Each proxy handler tries to stay on top of the pipeline
 * of its corresponding channel. Whenever a message is received on one channel, it is written to the other channel.
 * 
 * To configure a channel flange, you must configure the left and the right side of the flange. To do so, you have two
 * options:
 * 
 * i) Set the (left|right) channel using {@see #setLeftChannel(Channel)} or {@see #setRightChannel(Channel)}
 * 
 * ii) Get the (left|right) handler using {@see #getLeftHandler()} or {@see #getRigthHandler()} and add it to the top of
 * a pipeline
 * 
 * @author Dennis Pfisterer
 * 
 */
public class ChannelFlange {
    /** Used to generate the handler's name */
    private static final Random r = new Random();

    class ProxyHandler extends SimpleChannelHandler {
        private long id = r.nextLong();
        private Channel channel = null;
        private ProxyHandler otherProxyHandler;

        public void registerWith(Channel channel) {
            Preconditions.checkNotNull(channel);

            ChannelPipeline pipeline = channel.getPipeline();

            if (!(pipeline.getLast() == this)) {
                if (pipeline.toMap().entrySet().contains(this))
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
