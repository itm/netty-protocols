package de.uniluebeck.itm.netty.handlerstack.tinyos;

import net.tinyos.util.Crc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.uniluebeck.itm.netty.handlerstack.tinyos.TinyOsSerial.TYPE_ACTIVE_MESSAGE;

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
		final ChannelBuffer encoded = ChannelBuffers.buffer(decodedLength + 4);

		int crc = 0;

		encoded.writeByte(PACKET_TYPE);
		encoded.writeByte(TYPE_ACTIVE_MESSAGE);

		crc = Crc.calcByte(crc, PACKET_TYPE);
		crc = Crc.calcByte(crc, TYPE_ACTIVE_MESSAGE);

		byte currentByte;

		for (int i = 0; i < decodedLength; i++) {

			currentByte = decoded.readByte();
			encoded.writeByte(currentByte);
			crc = Crc.calcByte(crc, currentByte);
		}

		encoded.writeByte(crc & 0xFF);
		encoded.writeByte(crc >> 8);

		return encoded;
	}
}
