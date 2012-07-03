package de.uniluebeck.itm.netty.handlerstack.serialp;

import net.tinyos.util.Crc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class SerialPEncoder extends OneToOneEncoder {

	private static final int PACKET_TYPE = 0x45 & 0xFF;

	private static final int FIRST_BYTE = 0x00;

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		final ChannelBuffer decoded = (ChannelBuffer) msg;
		final int decodedLength = decoded.readableBytes();
		final ChannelBuffer encoded = ChannelBuffers.buffer(decodedLength + 4);

		int crc = 0;

		encoded.writeByte(PACKET_TYPE);
		encoded.writeByte(FIRST_BYTE);

		crc = Crc.calcByte(crc, PACKET_TYPE);
		crc = Crc.calcByte(crc, FIRST_BYTE);

		byte currentByte;
		final int decodedReaderIndex = decoded.readerIndex();

		for (int i = 0; i < decodedLength; i++) {

			currentByte = decoded.getByte(decodedReaderIndex + i);
			encoded.writeByte(currentByte);
			crc = Crc.calcByte(crc, currentByte);
		}

		encoded.writeByte(crc & 0xFF);
		encoded.writeByte(crc >> 8);

		return encoded;
	}
}
