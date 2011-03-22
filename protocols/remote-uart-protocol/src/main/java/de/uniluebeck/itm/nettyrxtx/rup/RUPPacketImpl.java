/**
 * Copyright (c) 2010, Dennis Pfisterer, Institute of Telematics, University of Luebeck
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
package de.uniluebeck.itm.nettyrxtx.rup;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.google.common.base.Preconditions;

import de.uniluebeck.itm.tr.util.StringUtils;

public class RUPPacketImpl implements RUPPacket {

    private final ChannelBuffer payload;

    private final byte cmdType;

    private final long destination;

    private final long source;

    public RUPPacketImpl(final byte cmdType, final long destination, final long source, final ChannelBuffer... payloads) {
        this(cmdType, destination, source, ChannelBuffers.wrappedBuffer(payloads));
    }

    public RUPPacketImpl(final Type cmdType, final long destination, final long source, final ChannelBuffer... payloads) {
        this(cmdType.getValue(), destination, source, ChannelBuffers.wrappedBuffer(payloads));
    }

    public RUPPacketImpl(final Type cmdType, final long destination, final long source, final ChannelBuffer payload) {
        this(cmdType.getValue(), destination, source, payload);
    }

    public RUPPacketImpl(final byte cmdType, final long destination, final long source, final ChannelBuffer payload) {

        Preconditions.checkNotNull(cmdType, "cmdType is null");
        Preconditions.checkNotNull(destination, "destination is null");
        Preconditions.checkNotNull(source, "source is null");
        // payload is allowed to be null in case somebody wants to send empty
        // packets

        this.cmdType = cmdType;
        this.destination = destination;
        this.source = source;
        this.payload = payload;

    }

    public byte getCmdType() {
        return cmdType;
    }

    public long getDestination() {
        return destination;
    }

    public long getSource() {
        return source;
    }

    public ChannelBuffer getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "RUPPacketImpl[" + "cmdType=" + cmdType + ",destination=" + destination + ",source=" + source
                + ",payload=" + StringUtils.toHexString(payload.array()) + ']';
    }
}
