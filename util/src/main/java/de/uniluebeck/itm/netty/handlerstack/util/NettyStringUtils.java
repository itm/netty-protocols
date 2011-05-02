package de.uniluebeck.itm.netty.handlerstack.util;

import org.jboss.netty.buffer.ChannelBuffer;

public class NettyStringUtils {

    /**
     * Returns a hex string representation of {@code channelBuffer}. The method will read the buffer without modifying
     * {@code readerIndex} and {@code writerIndex}.
     * 
     * @param channelBuffer
     *            the buffer
     * @param offset
     *            the index in the buffer where to start from
     * @param length
     *            the number of bytes in the buffer to read
     * 
     * @return a hex string representation of the buffer
     */
    public static String toHexString(final ChannelBuffer channelBuffer, int offset, int length) {
        StringBuffer s = new StringBuffer();
        for (int i = offset; i < offset + length; ++i) {
            if (s.length() > 0) {
                s.append(' ');
            }
            s.append("0x");
            s.append(Integer.toHexString(channelBuffer.getByte(i) & 0xFF));
        }
        return s.toString();
    }

    /**
     * Returns a hex string representation of {@code channelBuffer}. The method will read the buffer without modifying
     * {@code readerIndex} and {@code writerIndex}.
     * 
     * @param channelBuffer
     *            the buffer
     * 
     * @return a hex string representation of the buffer
     */
    public static String toHexString(final ChannelBuffer channelBuffer) {
        return toHexString(channelBuffer, channelBuffer.readerIndex(), channelBuffer.readableBytes());
    }

    /**
     * Returns a string representation of {@code channelBuffer}. The method will read the buffer without modifying
     * {@code readerIndex} and {@code writerIndex}.
     * 
     * @param channelBuffer
     *            the buffer
     * @param offset
     *            the index in the buffer where to start from
     * @param length
     *            the number of bytes in the buffer to read
     * 
     * @return a string representation of the buffer
     */
    public static String toString(final ChannelBuffer channelBuffer, int offset, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = offset; i < offset + length; i++) {
            builder.append(channelBuffer.getByte(i));
        }
        return builder.toString();
    }

    /**
     * Returns a string representation of {@code channelBuffer}. The method will read the buffer after wrapping it,
     * thereby not modifying {@code readerIndex} and {@code writerIndex}.
     * 
     * @param channelBuffer
     *            the buffer
     * 
     * @return a string representation of the buffer
     */
    public static String toString(final ChannelBuffer channelBuffer) {
        return toString(channelBuffer, channelBuffer.readerIndex(), channelBuffer.readableBytes());
    }
}