package de.uniluebeck.itm.netty.handlerstack.swap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class SwapResponse {

	public static class Factory {

		public static SwapResponse create(final long destination, final long source, final byte requestId,
										  final WiselibReturnValue wiselibReturnValue, final byte responseLength,
										  final byte[] responsePayload) {

			return new SwapResponse(destination, source, requestId, wiselibReturnValue, responseLength, responsePayload
			);
		}

		public static SwapResponse wrap(final long destination, final long source, final ChannelBuffer buffer) {

			return new SwapResponse(destination, source, buffer);
		}

	}

	private static final int POS_REQUEST_ID = 1;

	private static final int POS_CODE = 2;

	private static final int POS_PAYLOAD_LENGTH = 3;

	private static final int POS_PAYLOAD = 4;

	public static final byte PACKET_TYPE = 0x47; // 71 decimal

	private final ChannelBuffer buffer;

	private final long destination;

	private final long source;

	private SwapResponse(final long destination, final long source, final byte requestId,
						 final WiselibReturnValue wiselibReturnValue, final byte responseLength,
						 final byte[] responsePayload) {

		this.destination = destination;
		this.source = source;

		buffer = ChannelBuffers.buffer(1 + 1 + 1 + 1 + responsePayload.length);
		buffer.writeByte(PACKET_TYPE);
		buffer.writeByte(requestId);
		buffer.writeByte(wiselibReturnValue.getValue());
		buffer.writeByte(responseLength);
		buffer.writeBytes(responsePayload);
	}

	private SwapResponse(final long destination, final long source, final ChannelBuffer buffer) {

		this.destination = destination;
		this.source = source;

		this.buffer = ChannelBuffers.wrappedBuffer(buffer);
	}

	public byte getRequestId() {
		return buffer.getByte(POS_REQUEST_ID);
	}

	public WiselibReturnValue getReturnValue() {
		return WiselibReturnValue.fromValue(buffer.getByte(POS_CODE));
	}

	public ChannelBuffer getPayload() {
		return buffer.slice(POS_PAYLOAD, buffer.getByte(POS_PAYLOAD_LENGTH));
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append("de.uniluebeck.itm.netty.handlerstack.swap.SwapResponse[")
				.append("destination=")
				.append("0x").append(Long.toHexString(destination))
				.append(",source=")
				.append("0x").append(Long.toHexString(source))
				.append(",requestId=")
				.append("0x").append(Integer.toHexString(getRequestId() & 0xFF))
				.append(",returnValue=")
				.append(getReturnValue())
				.append("(")
				.append("0x")
				.append(Integer.toHexString(getReturnValue().getValue() & 0xFF))
				.append(")");

		if (hasPayload()) {
			builder.append(",payload=\"")
					.append(getPayload().toString(CharsetUtil.UTF_8).replaceAll("\n", "\\\\n"))
					.append("\"");
		}
		builder.append("]");
		return builder.toString();
	}

	private boolean hasPayload() {
		return buffer.readableBytes() > POS_PAYLOAD;
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
}
