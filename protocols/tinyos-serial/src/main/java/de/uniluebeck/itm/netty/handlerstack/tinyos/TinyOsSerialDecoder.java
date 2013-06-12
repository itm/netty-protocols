package de.uniluebeck.itm.netty.handlerstack.tinyos;

import net.tinyos.util.Crc;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.uniluebeck.itm.netty.handlerstack.tinyos.TinyOsSerial.PROTO_ACK;
import static de.uniluebeck.itm.netty.handlerstack.tinyos.TinyOsSerial.PROTO_PACKET_ACK;
import static de.uniluebeck.itm.netty.handlerstack.tinyos.TinyOsSerial.PROTO_PACKET_NOACK;

public class TinyOsSerialDecoder extends OneToOneDecoder {

	private final Logger log;

	public TinyOsSerialDecoder() {
		this(null);
	}

	public TinyOsSerialDecoder(final String instanceName) {
		log = LoggerFactory.getLogger(instanceName != null ? instanceName : TinyOsSerialDecoder.class.getName());
	}

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		final ChannelBuffer encoded = (ChannelBuffer) msg;
		final int encodedBufferLength = encoded.readableBytes();

		if (encodedBufferLength < 4) {
			throw new IllegalArgumentException("Packet must be at least 4 bytes long!");
		}

		final byte packetType = encoded.getByte(0);
		final byte sequenceNumber = encoded.getByte(1);

		int payloadBegin = encoded.readerIndex() + 2;
		int payloadEnd = encoded.writerIndex() - 2;

		switch (packetType) {
			case PROTO_PACKET_NOACK:
				// nothing to do as packet does not need to be acknowledged
				break;
			case PROTO_PACKET_ACK:
				// TODO implement acknowledgment of packet
				break;
			case PROTO_ACK:
				// TODO what to do here?
				break;
			default:
				throw new IllegalArgumentException("TinyOsSerialDecoder received unknown packet type: " + packetType);
		}

		final ChannelBuffer decoded = ChannelBuffers.buffer(encodedBufferLength - 3);

		int crcRead = (encoded.getByte(encoded.readerIndex() + encoded.readableBytes() - 2) & 0xff)
				| (encoded.getByte(encoded.readerIndex() + encoded.readableBytes() - 1) & 0xff) << 8;

		int crcCalculated = 0;
		crcCalculated = Crc.calcByte(crcCalculated, packetType);
		crcCalculated = Crc.calcByte(crcCalculated, sequenceNumber);

		byte currentByte;
		for (int i = payloadBegin; i < payloadEnd; i++) {
			currentByte = encoded.getByte(i);
			decoded.writeByte(currentByte);
			crcCalculated = Crc.calcByte(crcCalculated, currentByte);
		}

		if (crcCalculated != crcRead) {
			log.warn("CRC mismatch, discarding packet {}", decoded);
			return null;
		}

		return decoded;
	}
}
