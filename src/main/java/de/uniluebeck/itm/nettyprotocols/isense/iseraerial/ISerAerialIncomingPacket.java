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
package de.uniluebeck.itm.nettyprotocols.isense.iseraerial;

import de.uniluebeck.itm.nettyprotocols.util.NettyStringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.LoggerFactory;

public class ISerAerialIncomingPacket {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ISerAerialIncomingPacket.class);

	private static final int HEADER_LENGTH = 9;

	public static final byte TYPE_CODE = 0x00;

	private final ChannelBuffer buffer;

	/**
	 * Creates a new ISensePacket instance using {@code buffer} as its backing buffer.
	 *
	 * @param buffer
	 * 		the backing buffer
	 */
	public ISerAerialIncomingPacket(ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() < HEADER_LENGTH) {
			throw new Exception(String.format("Packet size of %d is too short, expecting at least %d",
					buffer.readableBytes(), HEADER_LENGTH
			)
			);
		}

		if (buffer.getByte(0) != TYPE_CODE) {
			throw new Exception(String.format("Unexpected first type byte %d, expected %d", buffer.getByte(0),
					TYPE_CODE
			)
			);
		}

		this.buffer = buffer;
	}

	public int getSource() {
		return ((0xFF & buffer.getByte(1)) << 8) + (0xFF & buffer.getByte(2));
	}

	public int getDestination() {
		return ((0xFF & buffer.getByte(3)) << 8) + (0xFF & buffer.getByte(4));
	}

	public int getLinkQualityIndicator() {
		return ((0xFF & buffer.getByte(5)) << 8) + (0xFF & buffer.getByte(6));
	}

	public int getInterface() {
		return (0xFF & buffer.getByte(7));
	}

	public ChannelBuffer getPayload() {
		int readablePayloadBytes = buffer.readableBytes() - HEADER_LENGTH;
		int advertisedPayloadBytes = (0xFF & buffer.getByte(8));

		if (readablePayloadBytes == 0 || readablePayloadBytes != advertisedPayloadBytes) {
			log.warn("Unable to decode packet, advertised payload length {} != readableBytes {}",
					advertisedPayloadBytes, readablePayloadBytes
			);
			return ChannelBuffers.EMPTY_BUFFER;
		}

		if (readablePayloadBytes == 0) {
			log.debug("Empty payload received, returning emptyp buffer");
			return ChannelBuffers.EMPTY_BUFFER;
		}

		return ChannelBuffers.unmodifiableBuffer(buffer.slice(HEADER_LENGTH, buffer.readableBytes() - HEADER_LENGTH));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ISerAerialIncomingPacket [source=0x");
		builder.append(Integer.toString(getSource(), 16));
		builder.append(", destination=0x");
		builder.append(Integer.toString(getDestination(), 16));
		builder.append(", lqi=");
		builder.append(getLinkQualityIndicator());
		builder.append(", interface=");
		builder.append(getInterface());
		builder.append(", payload=");
		builder.append(NettyStringUtils.toHexString(getPayload()));
		builder.append("]");
		return builder.toString();
	}

}
