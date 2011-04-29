/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.netty.handlerstack.swap;

import de.uniluebeck.itm.nettyrxtx.rup.RUPPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.util.CharsetUtil;

import java.math.BigInteger;


public class SwapResponseDecoder extends OneToOneDecoder {

	private static final byte SENSOR_VALUE_ENCODING_BOOL = 0x01;

	private static final byte SENSOR_VALUE_ENCODING_UINT8 = 0x02;

	private static final byte SENSOR_VALUE_ENCODING_INT8 = 0x03;

	private static final byte SENSOR_VALUE_ENCODING_UINT16 = 0x04;

	private static final byte SENSOR_VALUE_ENCODING_INT16 = 0x05;

	private static final byte SENSOR_VALUE_ENCODING_UINT32 = 0x06;

	private static final byte SENSOR_VALUE_ENCODING_INT32 = 0x07;

	private static final byte SENSOR_VALUE_ENCODING_UINT64 = 0x08;

	private static final byte SENSOR_VALUE_ENCODING_INT64 = 0x09;

	private static final byte SENSOR_VALUE_ENCODING_STRING = 0x0a;

	private static final byte SENSOR_VALUE_ENCODING_DOUBLE = 0x0b;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (!(msg instanceof RUPPacket)) {
			return msg;
		}

		RUPPacket packet = (RUPPacket) msg;

		if (RUPPacket.Type.MESSAGE.getValue() != packet.getCmdType()) {
			return msg;
		}

		return SwapResponse.Factory.wrap(packet.getDestination(), packet.getSource(), packet.getPayload());
	}

	/**
	 * Interprets the payload of {@code response} as sensor value as defined in the SWAP protocol, parses and returns it.
	 *
	 * @param response the response packet
	 *
	 * @return the response packets payload parsed as byte, short, int, long, BigInteger, double, String or boolean
	 */
	public static Object decodeResponseSensorValue(SwapResponse response) {
		ChannelBuffer payload = response.getPayload();
		short dataLength = payload.getUnsignedByte(1);
		switch (payload.getByte(0)) {
			case SENSOR_VALUE_ENCODING_BOOL:
				return payload.getByte(2) == 0;
			case SENSOR_VALUE_ENCODING_DOUBLE:
				return payload.getDouble(2);
			case SENSOR_VALUE_ENCODING_INT8:
				return payload.getByte(2);
			case SENSOR_VALUE_ENCODING_INT16:
				return payload.getShort(2);
			case SENSOR_VALUE_ENCODING_INT32:
				return payload.getInt(2);
			case SENSOR_VALUE_ENCODING_INT64:
				return payload.getLong(2);
			case SENSOR_VALUE_ENCODING_STRING:
				return payload.toString(2, dataLength, CharsetUtil.UTF_8);
			case SENSOR_VALUE_ENCODING_UINT8:
				return payload.getUnsignedByte(2);
			case SENSOR_VALUE_ENCODING_UINT16:
				return payload.getUnsignedShort(2);
			case SENSOR_VALUE_ENCODING_UINT32:
				return payload.getUnsignedInt(2);
			case SENSOR_VALUE_ENCODING_UINT64:
				byte[] data = new byte[dataLength];
				payload.getBytes(2, data);
				return new BigInteger(data);
		}
		return null;
	}

}
