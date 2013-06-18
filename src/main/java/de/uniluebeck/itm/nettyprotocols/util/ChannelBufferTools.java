package de.uniluebeck.itm.nettyprotocols.util;

import de.uniluebeck.itm.util.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;

@SuppressWarnings("unused")
public abstract class ChannelBufferTools {

	/**
	 * Uses {@link ChannelBuffer#getBytes(int, byte[], int, int)} to copy the buffers contents to a byte array.
	 *
	 * @param buffer
	 * 		the buffer to copy from
	 *
	 * @return a new byte array instance
	 */
	public static byte[] getToByteArray(final ChannelBuffer buffer) {
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.getBytes(buffer.readerIndex(), bytes, 0, buffer.readableBytes());
		return bytes;
	}

	/**
	 * Uses {@link ChannelBuffer#readBytes(byte[])} to copy the buffers contents to a byte array.
	 *
	 * @param buffer
	 * 		the buffer to copy from
	 *
	 * @return a new byte array instance
	 */
	public static byte[] readToByteArray(final ChannelBuffer buffer) {
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		return bytes;
	}

	/**
	 * Same as calling {@code toPrintableString(buffer, Integer.MAX_VALUE)}.
	 *
	 * @param buffer
	 * 		the buffer to convert
	 *
	 * @return a printable String
	 */
	public static String toPrintableString(final ChannelBuffer buffer) {
		return toPrintableString(buffer, Integer.MAX_VALUE);
	}

	/**
	 * Returns a printable (ASCII) String by constructing a new String of maximum length {@code maxLength} and calling
	 * {@link StringUtils#replaceNonPrintableAsciiCharacters(String)} on it.
	 *
	 * @param buffer
	 * 		the buffer to convert
	 * @param maxLength
	 * 		the maximum length of the input String for {@link StringUtils#replaceNonPrintableAsciiCharacters(String)}
	 *
	 * @return a printable String
	 */
	public static String toPrintableString(final ChannelBuffer buffer, int maxLength) {
		int actualLength = buffer.readableBytes() < maxLength ? buffer.readableBytes() : maxLength;
		final byte[] bytes = new byte[actualLength];
		buffer.getBytes(buffer.readerIndex(), bytes, 0, actualLength);
		return StringUtils.replaceNonPrintableAsciiCharacters(new String(bytes));
	}

}
