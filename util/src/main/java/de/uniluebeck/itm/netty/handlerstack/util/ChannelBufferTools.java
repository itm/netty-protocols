package de.uniluebeck.itm.netty.handlerstack.util;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class ChannelBufferTools {

	/**
	 * Uses {@link ChannelBuffer#readBytes(byte[])} to copy the buffers contents to a byte array.
	 *
	 * @param buffer the buffer to copy from
	 * @return a new byte array instance
	 */
	public static byte[] readToByteArray(final ChannelBuffer buffer) {
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		return bytes;
	}

	/**
	 * Uses {@link ChannelBuffer#getBytes(int, byte[], int, int)} to copy the buffers contents to a byte array.
	 *
	 * @param buffer the buffer to copy from
	 * @return a new byte array instance
	 */
	public static byte[] getToByteArray(final ChannelBuffer buffer) {
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.getBytes(0, bytes, 0, buffer.readableBytes());
		return bytes;
	}

}
