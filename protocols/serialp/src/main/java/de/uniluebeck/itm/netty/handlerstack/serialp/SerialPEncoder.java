package de.uniluebeck.itm.netty.handlerstack.serialp;

import net.tinyos.util.Crc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class SerialPEncoder extends OneToOneEncoder {

	/**
	 * Original code:
	 * <p/>
	 * <pre>
	 * inline uint16_t rx_current_crc() {
	 *   uint16_t crc;
	 *   uint8_t tmp = rxBuf.writePtr;
	 *   tmp = (tmp == 0 ? RX_DATA_BUFFER_SIZE : tmp - 1);
	 *   crc = rxBuf.buf[tmp] & 0x00ff;
	 *   crc = (crc << 8) & 0xFF00;
	 *   tmp = (tmp == 0 ? RX_DATA_BUFFER_SIZE : tmp - 1);
	 *   crc |= (rxBuf.buf[tmp] & 0x00FF);
	 *   return crc;
	 * }
	 * </pre>
	 *
	 * @param ctx
	 * @param channel
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		/*final ChannelBuffer decoded = ChannelBuffers.buffer(encodedBufferLength - 4);
		int crc = 0;

		decoded.setByte(0, (0x45 & 0xFF)); // protocol field (0x45 = SerialP)
		decoded.setByte(1, (0x00 & 0xFF)); // sequence number (not used)

		final int readerIndex = encoded.readerIndex();
		byte currentByte;
		for (int i = 0; i < encoded.readableBytes(); i++) {
			currentByte = encoded.getByte(readerIndex + i);
			decoded.setByte(2 + i, currentByte);
		}

		decoded.setShort(encodedBufferLength + 2, crc); // crc

		decoded.writerIndex(encodedBufferLength + 4);

		return decoded;*/

		return null;
	}
}
