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
package de.uniluebeck.itm.netty.handlerstack.iseraerial;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacket;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketType;

public class ISerAerialPacketDecoder extends OneToOneDecoder {

    private final Logger log;

    /**
     * Package-private constructor for creation via factory only
     */
    ISerAerialPacketDecoder() {
        this(null);
    }
    /**
     * Package-private constructor for creation via factory only
     */
    ISerAerialPacketDecoder(String instanceName) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : ISerAerialPacketDecoder.class.getName());
    }

    @Override
    protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

        if (!(msg instanceof ISensePacket)) {
            return msg;
        }
        
        ISensePacket iSensePacket = (ISensePacket) msg;
        
        if( iSensePacket.getType() != ISensePacketType.SERAERIAL.getValue())
            return msg;

        ChannelBuffer payload = iSensePacket.getPayload();
        
        if (!payload.readable())
            return msg;

        //First byte of the payload is iSerAerial's subtype (packet or confirm)
        int subtypeCode = payload.getByte(0);

        if (subtypeCode == ISerAerialIncomingPacket.TYPE_CODE) {
            ISerAerialIncomingPacket packet = new ISerAerialIncomingPacket(payload);
            log.trace("Decoded incoming iSerAerial data packet: {}", packet);
            return packet;

        } else if (subtypeCode == ISerAerialConfirmPacket.TYPE_CODE) {
            ISerAerialConfirmPacket packet = new ISerAerialConfirmPacket(payload);
            log.trace("Decoded incoming iSerAerial confirm packet: {}", packet);
            return packet;
        }

        log.warn("Unable to decode iSerAerial packet, unknown type code {} as first byte", subtypeCode);
        return msg;
    }
}
