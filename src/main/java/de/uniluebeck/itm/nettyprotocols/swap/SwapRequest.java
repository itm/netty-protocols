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
package de.uniluebeck.itm.nettyprotocols.swap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class SwapRequest {

	public static enum Type {

		GET_SENSORS((byte) 0x00),
		GET_SENSORNODE_META_DATA((byte) 0x01),
		GET_SENSOR_VALUE((byte) 0x02),
		GET_SENSOR_META_DATA((byte) 0x03),
		SUBSCRIBE((byte) 0x04),
		UNSUBSCRIBE((byte) 0x05),
		PUT_SENSOR_META_DATA((byte) 0x06);

		private byte value;

		private Type(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		public static Type fromValue(final byte b) {
			for (Type type : Type.values()) {
				if (type.getValue() == b) {
					return type;
				}
			}
			throw new IllegalArgumentException("Unknown Type");
		}

	}

	public static class Factory {

		public static SwapRequest create(final long destination, final long source, final byte requestId,
										 final Type type) {

			return new SwapRequest(destination, source, requestId, type, null);
		}

		public static SwapRequest create(final long destination, final long source, final byte requestId,
										 final Type type, byte[] requestOptions) {

			return new SwapRequest(destination, source, requestId, type, requestOptions);
		}

		public static SwapRequest wrap(final long destination, final long source, final ChannelBuffer buffer) {

			return new SwapRequest(destination, source, buffer);
		}

	}

	public static final byte PACKET_TYPE = 0x46; // 70 decimal

	private static final int POS_REQUEST_ID = 1;

	private static final int POS_REQUEST_TYPE = 2;

	private static final int POS_REQUEST_OPTIONS_LENGTH = 3;

	private static final int POS_REQUEST_OPTIONS = 4;

	private final long destination;

	private final long source;

	private ChannelBuffer buffer;

	private SwapRequest(final long destination, final long source, final byte requestId, final Type type,
						final byte[] requestOptions) {

		this.destination = destination;
		this.source = source;

		buffer = ChannelBuffers.buffer(1 + 1 + 1 + 1 + (requestOptions == null ? 0 : requestOptions.length));
		buffer.writeByte(PACKET_TYPE);
		buffer.writeByte(requestId);
		buffer.writeByte(type.getValue());
		buffer.writeByte((byte) (0xFF & (requestOptions == null ? 0 : requestOptions.length)));
		if (requestOptions != null) {
			buffer.writeBytes(requestOptions);
		}
	}

	private SwapRequest(final long destination, final long source, final ChannelBuffer bufferToWrap) {

		this.destination = destination;
		this.source = source;

		buffer = ChannelBuffers.wrappedBuffer(bufferToWrap);
	}

	public byte getRequestId() {
		return buffer.getByte(POS_REQUEST_ID);
	}

	public Type getRequestType() {
		return Type.fromValue(buffer.getByte(POS_REQUEST_TYPE));
	}

	public ChannelBuffer getRequestOptions() {
		if (hasRequestOptions()) {
			return buffer.slice(POS_REQUEST_OPTIONS, buffer.getByte(POS_REQUEST_OPTIONS_LENGTH));
		}
		return ChannelBuffers.EMPTY_BUFFER;
	}

	private boolean hasRequestOptions() {
		return buffer.readableBytes() > POS_REQUEST_OPTIONS + 1;
	}

	public ChannelBuffer getBuffer() {
		return buffer;
	}

	public long getDestination() {
		return destination;
	}

	public long getSource() {
		return source;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append("de.uniluebeck.itm.netty.handlerstack.swap.SwapRequest[")
				.append("destination=")
				.append("0x").append(Long.toHexString(destination))
				.append(",source=")
				.append("0x").append(Long.toHexString(source))
				.append(",requestId=")
				.append("0x").append(Integer.toHexString(getRequestId() & 0xFF))
				.append(",type=")
				.append(getRequestType())
				.append("(")
				.append("0x")
				.append(Integer.toHexString(getRequestType().getValue() & 0xFF))
				.append(")");

		if (hasRequestOptions()) {
			builder.append(",requestOptions=\"")
					.append(getRequestOptions().toString(CharsetUtil.UTF_8).replaceAll("\n", "\\\\n"))
					.append("\"");
		}

		builder.append("]");

		return builder.toString();
	}
}
