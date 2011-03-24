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
package de.uniluebeck.itm.netty.handlerstack.dlestxetx;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.tr.util.StringUtils;

public class DleStxEtxFramingDecoder extends FrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(DleStxEtxFramingDecoder.class);

    private boolean foundDLE;

    private boolean foundPacket;

    private ChannelBuffer packet;

    /**
     * Package-private constructor for creation via factory only
     */
    DleStxEtxFramingDecoder() {
        resetPacketState();
    }

    @Override
    protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer buffer)
            throws Exception {

        while (buffer.readable()) {

            byte c = buffer.readByte();

            // check if last character read was DLE
            if (foundDLE) {
                foundDLE = false;

                if (c == DleStxEtxConstants.STX && !foundPacket) {

                    // log.trace("STX received in DLE mode");
                    foundPacket = true;

                } else if (c == DleStxEtxConstants.ETX && foundPacket) {

                    // packet was completely received
                    if (log.isTraceEnabled()) {
                        log.trace(
                                "[{}] Packet decoding completed: {}",
                                new Object[] {
                                        ctx.getName(),
                                        StringUtils.toHexString(packet.array(), packet.readerIndex(),
                                                packet.readableBytes()) });
                    }
                    ChannelBuffer packetRead = packet;
                    resetPacketState();
                    return packetRead;

                } else if (c == DleStxEtxConstants.DLE && foundPacket) {

                    // Stuffed DLE found
                    // log.trace("Stuffed DLE received in DLE mode");
                    packet.writeByte(DleStxEtxConstants.DLE);

                } else {

                    if (log.isWarnEnabled()) {
                        log.warn("Incomplete packet received: {}",
                                StringUtils.toHexString(packet.array(), packet.readerIndex(), packet.readableBytes()));
                    }
                    resetPacketState();
                }

            } else {

                if (c == DleStxEtxConstants.DLE) {
                    // log.trace("Plain DLE received");
                    foundDLE = true;
                } else if (foundPacket) {
                    packet.writeByte(c);
                }

            }
        }

        // decoding is not yet complete, we'll need more bytes until we find DLE
        // ETX
        return null;
    }

    private void resetPacketState() {
        foundDLE = false;
        foundPacket = false;
        packet = ChannelBuffers.dynamicBuffer(512);
    }

}
