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
package de.uniluebeck.itm.nettyrxtx.isense;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import de.uniluebeck.itm.tr.util.StringUtils;

public class ISensePacket {

	private final ChannelBuffer buffer;

	/**
	 * Creates a new ISensePacket instance that creates a new wrapping ChannelBuffer around {@code type} and
	 * {@code payload}. Any changes made to {@code payload} will reflect on this packet.
	 *
	 * @param type	the type of the packet (see {@link ISensePacketType})
	 * @param payload the packets payload
	 * @see ChannelBuffers#wrappedBuffer(java.nio.ByteBuffer...)
	 */
	public ISensePacket(final byte type, final ChannelBuffer payload) {
		this.buffer = ChannelBuffers.wrappedBuffer(ChannelBuffers.wrappedBuffer(new byte[]{type}), payload);
	}

	/**
	 * Creates a new ISensePacket instance using {@code buffer} as its backing buffer.
	 *
	 * @param buffer the backing buffer
	 */
	public ISensePacket(ChannelBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Creates a new ISensePacket by calling {@code this(type.getValue(), channelBuffer)}.
	 *
	 * @param type the type of the packet
	 * @param channelBuffer the packets payload
	 */
	public ISensePacket(ISensePacketType type, ChannelBuffer channelBuffer) {
		this(type.getValue(), channelBuffer);
	}

	/**
	 * Returns a slice of the underlying {@link ChannelBuffer} containing the packets payload.
	 *
	 * @return a slice of the underlying ChannelBuffer
	 * @see ChannelBuffer#slice(int, int)
	 */
	public ChannelBuffer getPayload() {
		return buffer.slice(1, buffer.readableBytes() - 1);
	}

	/**
	 * Returns the {@code type} byte of the packet. This method does not modify the ChannelBuffer backing this packet.
	 *
	 * @return the type byte
	 */
	public byte getType() {
		return buffer.getByte(0);
	}

	@Override
	public String toString() {
		ISensePacketType packetType = ISensePacketType.fromValue(getType());
		StringBuilder builder = new StringBuilder();
		builder.append("ISensePacket[type=");
		builder.append(packetType == null ? getType() : packetType);
		builder.append(",payload=");
		if (packetType == ISensePacketType.LOG) {
			ChannelBuffer buffer = getPayload();
			byte[] payloadBuffer = new byte[buffer.readableBytes()];
			buffer.readBytes(payloadBuffer);
			String s = new String(payloadBuffer);
			builder.append(s.endsWith("\n") ? s.substring(0, s.length() - 2) : s);
		} else {
			builder.append(StringUtils.toHexString(getPayload().array()));
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the buffer backing this packet. <i>Please note:</i> This is the original buffer, any modifications made
	 * to it will reflect on the packet.
	 *
	 * @return this packets backing buffer
	 */
	public ChannelBuffer getBuffer() {
		return buffer;
	}
}
