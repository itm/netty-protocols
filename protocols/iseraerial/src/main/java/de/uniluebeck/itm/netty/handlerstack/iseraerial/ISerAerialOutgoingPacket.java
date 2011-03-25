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
import org.jboss.netty.buffer.ChannelBuffers;

public class ISerAerialOutgoingPacket {
    private static final int HEADER_LENGTH = 4;
    private final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(HEADER_LENGTH + 127);

    public ISerAerialOutgoingPacket() {
        buffer.writeZero(HEADER_LENGTH);
    }

    public int getDestination() {
        return ((0xFF & buffer.getByte(0)) << 8) + (0xFF & buffer.getByte(1));
    }

    public void setDestination(int destination) {
        buffer.setByte(0, (byte) ((destination >> 8) & 0xFF));
        buffer.setByte(1, (byte) (destination & 0xFF));
    }

    public int getOptions() {
        return buffer.getByte(2);
    }

    public void setOptions(int options) {
        buffer.setByte(2, options & 0xFF);
    }

    public ChannelBuffer getPayload() {
        return buffer.slice(HEADER_LENGTH, buffer.readableBytes() - HEADER_LENGTH);
    }

    private void setLength(int length) {
        buffer.setByte(3, 0xFF & length);
    }

    public ChannelBuffer getPacket() {
        setLength(buffer.readableBytes());
        return ChannelBuffers.unmodifiableBuffer(buffer);
    }

}
