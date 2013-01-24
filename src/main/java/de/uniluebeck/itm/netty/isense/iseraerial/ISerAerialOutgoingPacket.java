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

import de.uniluebeck.itm.netty.util.NettyStringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class ISerAerialOutgoingPacket {
    public static final short BROADCAST_ADDRESS_16_BIT = (short) 0xFFFF;

    private static final int SERAERIAL_HEADER_LENGTH = 4;

    private final ChannelBuffer buffer;

    public ISerAerialOutgoingPacket(short destination, byte options, ChannelBuffer payload) {

        ChannelBuffer headerBuffer = ChannelBuffers.buffer(SERAERIAL_HEADER_LENGTH);

        {// Set destination (bytes 0-1)
            headerBuffer.writeByte((byte) ((destination >> 8) & 0xFF));
            headerBuffer.writeByte((byte) (destination & 0xFF));
        }

        {// Set option field  (byte 2)
            headerBuffer.writeByte(options & 0xFF);
        }

        {// Set length field (length of the payload) -  (byte 3)
            int length = payload.readableBytes();
            headerBuffer.writeByte(0xFF & length);
        }

        buffer = ChannelBuffers.wrappedBuffer(headerBuffer, payload);
    }

    /** Returns the (unmodifiable) buffer that backs this packet */
    public ChannelBuffer getBuffer() {
        return ChannelBuffers.unmodifiableBuffer(buffer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ISerAerialOutgoingPacket [buffer=");
        builder.append(NettyStringUtils.toHexString(buffer));
        builder.append("]");
        return builder.toString();
    }

}
