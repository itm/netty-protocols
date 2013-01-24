package de.uniluebeck.itm.netty.tinyos;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TinyOsSerialEncoder extends OneToOneEncoder {

	private static final int PACKET_TYPE = 0x45 & 0xFF;

	private final Logger log;

	public TinyOsSerialEncoder() {
		this(null);
	}

	public TinyOsSerialEncoder(final String instanceName) {
		log = LoggerFactory.getLogger(instanceName != null ? instanceName : TinyOsSerialEncoder.class.getName());
	}

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		final ChannelBuffer decoded = (ChannelBuffer) msg;
		final int decodedLength = decoded.readableBytes();
		final ChannelBuffer encoded = ChannelBuffers.buffer(decodedLength + 3);

		int crc = 0;

		encoded.writeByte(PACKET_TYPE);

		final byte firstByte = decoded.readByte();
		encoded.writeByte(firstByte);

		crc = TinyOsCrc.calcByte(crc, PACKET_TYPE);
		crc = TinyOsCrc.calcByte(crc, firstByte);

		byte currentByte;

		for (int i = 1; i < decodedLength; i++) {

			currentByte = decoded.readByte();
			encoded.writeByte(currentByte);
			crc = TinyOsCrc.calcByte(crc, currentByte);
		}

		encoded.writeByte(crc & 0xFF);
		encoded.writeByte(crc >> 8);

		return encoded;
	}
}
