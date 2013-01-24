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
package de.uniluebeck.itm.netty.isense.iseraerial;

import de.uniluebeck.itm.netty.isense.ISensePacket;
import de.uniluebeck.itm.netty.isense.ISensePacketType;
import de.uniluebeck.itm.netty.util.StopAndWaitPacketSender;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ISerAerialPacketEncoder extends SimpleChannelHandler {
    private final Logger log;

    private final StopAndWaitPacketSender<ISensePacket> queue;

    public ISerAerialPacketEncoder() {
        this(null);
    }

    public ISerAerialPacketEncoder(String instanceName) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : ISerAerialPacketEncoder.class.getName());

        queue =
                new StopAndWaitPacketSender<ISensePacket>(instanceName + "-queue",
                        Executors.newSingleThreadScheduledExecutor(), 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (!(e.getMessage() instanceof ISerAerialConfirmPacket)) {
            super.messageReceived(ctx, e);
            return;
        }

        queue.confirmReceived();
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();

        if (!(msg instanceof ISerAerialOutgoingPacket)) {
            super.writeRequested(ctx, e);
            return;
        }

        ISerAerialOutgoingPacket packet = (ISerAerialOutgoingPacket) msg;
        ISensePacket iSensePacket = new ISensePacket(ISensePacketType.SERAERIAL.getValue(), packet.getBuffer());
        
        log.trace("Encoded and enqueued ISerAerialOutgoingPacket: {}", packet);
        queue.enqeue(iSensePacket, ctx, e.getFuture());
    }

}
