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
package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalesenses.tools.iSenseAes;
import com.coalesenses.tools.iSenseAes128BitKey;

import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.MacroFabricSerializer;
import de.uniluebeck.itm.netty.handlerstack.iseraerial.ISerAerialIncomingPacket;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;

public class ISenseOtapPacketDecoder extends SimpleChannelHandler {
    private final Logger log;

    private iSenseAes aes = null;

    /**
     * Package-private constructor for creation via factory only
     */
    ISenseOtapPacketDecoder() {
        this(null);
    }

    /**
     * Package-private constructor for creation via factory only
     */
    ISenseOtapPacketDecoder(String instanceName) {
        log = LoggerFactory.getLogger(instanceName != null ? instanceName : ISenseOtapPacketDecoder.class.getName());
        log.debug("New instance");
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();

        if (msg instanceof ISenseOtapPacketDecoderSetAESKeyRequest) {
            iSenseAes128BitKey key = ((ISenseOtapPacketDecoderSetAESKeyRequest) msg).getKey();
            if (key != null) {
                log.info("Decrypting payload of otap packets");
                this.aes = new iSenseAes(key);
            } else {
                log.info("Disabled otap payload encryption");
                this.aes = null;
            }
        } else {
            super.writeRequested(ctx, e);
        }

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (!(msg instanceof ISerAerialIncomingPacket)) {
            super.messageReceived(ctx, e);
            return;
        }

        ISerAerialIncomingPacket serAerialMsg = (ISerAerialIncomingPacket) msg;

        if (!serAerialMsg.getPayload().readable()) {
            super.messageReceived(ctx, e);
            return;
        }

        ChannelBuffer payload = serAerialMsg.getPayload();

        // Check if this is an OTAP packet
        if (payload.getByte(0) != ISenseOtapPacketType.OTAP) {
            super.messageReceived(ctx, e);
            return;
        }

        // Convert the payload to byte[]
        byte[] byteArray = new byte[payload.readableBytes()];
        payload.getBytes(payload.readerIndex(), byteArray, 0, byteArray.length);

        // Check if we need to decrypt the payload
        if (aes != null) {
            log.trace(("Encrypted otap payload wit AES"));
            byteArray = aes.decode(byteArray);

            if (byteArray == null) {
                log.trace("Unable to decrypt otap message. Message dropped");
                return;
            }
        }

        // Try to deserialize, if not, null is returned.
        Object result = MacroFabricSerializer.deserialize(byteArray);

        // Not an OTAP packet, return the original object
        if (result == null) {
            super.messageReceived(ctx, e);
            return;
        }

        // Return the decoded OTAP object
        HandlerTools.sendUpstream(result, ctx);
    }
}
