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
package de.uniluebeck.itm.netty.isense.otap;

import de.uniluebeck.itm.netty.isense.iseraerial.ISerAerialOutgoingPacket;
import de.uniluebeck.itm.netty.util.HandlerTools;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalesenses.tools.iSenseAes;
import com.coalesenses.tools.iSenseAes128BitKey;

import de.uniluebeck.itm.netty.isense.otap.generatedmessages.MacroFabricSerializer;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapInitReply;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapInitRequest;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapProgramReply;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.OtapProgramRequest;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.PresenceDetectReply;
import de.uniluebeck.itm.netty.isense.otap.generatedmessages.PresenceDetectRequest;

public class ISenseOtapPacketEncoder extends SimpleChannelDownstreamHandler {

    private final Logger log;

    private iSenseAes aes = null;

    public ISenseOtapPacketEncoder() {
        this(null);
    }

    public ISenseOtapPacketEncoder(String instanceName) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : ISenseOtapPacketEncoder.class.getName());
    }

    private byte[] computeCrc(byte[] payload) {
        byte crc[] = { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA };

        for (int i = 0; i < payload.length; i++)
            crc[i % 4] = (byte) (0xFF & (crc[i % 4] ^ payload[i]));

        return crc;
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();

        if (msg instanceof ISenseOtapPacketEncoderSetAESKeyRequest) {

            iSenseAes128BitKey key = ((ISenseOtapPacketEncoderSetAESKeyRequest) msg).getKey();

            if (key != null) {
                log.info("Encrypting payload of otap packets");
                this.aes = new iSenseAes(key);
            } else {
                log.info("Disabled otap payload encryption");
                this.aes = null;
            }

            return;
        }

        byte[] bytes = null;

        if (msg instanceof OtapInitReply)
            bytes = MacroFabricSerializer.serialize((OtapInitReply) msg);
        else if (msg instanceof OtapInitRequest)
            bytes = MacroFabricSerializer.serialize((OtapInitRequest) msg);
        else if (msg instanceof OtapProgramReply)
            bytes = MacroFabricSerializer.serialize((OtapProgramReply) msg);
        else if (msg instanceof OtapProgramRequest)
            bytes = MacroFabricSerializer.serialize((OtapProgramRequest) msg);
        else if (msg instanceof PresenceDetectRequest)
            bytes = MacroFabricSerializer.serialize((PresenceDetectRequest) msg);
        else if (msg instanceof PresenceDetectReply)
            bytes = MacroFabricSerializer.serialize((PresenceDetectReply) msg);

        if (bytes != null) {

            if (aes != null) {
                log.trace(("Encrypted otap payload wit AES"));
                bytes = aes.encodeWithRandomNonce(bytes);
            }

            ISerAerialOutgoingPacket iSerAerialPacket =
                    new ISerAerialOutgoingPacket(ISerAerialOutgoingPacket.BROADCAST_ADDRESS_16_BIT, (byte) 0x0,
                            ChannelBuffers.wrappedBuffer(bytes, computeCrc(bytes)));

            log.trace("Encoded {} otap packet with crc: {}", msg.getClass().getSimpleName(), iSerAerialPacket);

            HandlerTools.sendDownstream(iSerAerialPacket, ctx);
        } else {
            super.writeRequested(ctx, e);
        }

    }
}
