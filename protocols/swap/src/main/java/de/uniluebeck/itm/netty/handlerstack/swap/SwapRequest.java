package de.uniluebeck.itm.netty.handlerstack.swap;

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
