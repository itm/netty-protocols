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
package de.uniluebeck.itm.nettyrxtx.rup;


import com.google.common.base.Preconditions;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacket;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketType;

public class RUPHelper {

	/**
	 * Checks if {@code type} is one of {@link RUPPacket.Type#MESSAGE}, {@link RUPPacket.Type#SINK_REQUEST}
	 * or {@link RUPPacket.Type#SINK_RESPONSE}.
	 *
	 * @param type the byte indicating the packet type
	 *
	 * @return {@code true} if type is a remote UART packet type, {@code false} otherwise
	 */
	public static boolean isRemoteUARTPacket(byte type) {
		Preconditions.checkNotNull(type);
		return type == RUPPacket.Type.MESSAGE.getValue() ||
				type == RUPPacket.Type.SINK_REQUEST.getValue() ||
				type == RUPPacket.Type.SINK_RESPONSE.getValue() ||
				type == RUPPacket.Type.SET_SINK.getValue();
	}

	/**
	 * Checks if the first byte of {@code bytes} is one of {@link RUPPacket.Type#MESSAGE}, {@link
	 * RUPPacket.Type#SINK_REQUEST} or {@link RUPPacket.Type#SINK_RESPONSE}.
	 *
	 * @param bytes the packets byte-array representation
	 *
	 * @return {@code true} if the packets type is a remote UART packet type, {@code false} otherwise
	 */
	public static boolean isRemoteUARTPacket(byte[] bytes) {
		Preconditions.checkNotNull(bytes);
		Preconditions.checkArgument(bytes.length > 0);
		return isRemoteUARTPacket(bytes[0]);
	}

	/**
	 * Checks if the {@code iSensePacket} contains a Remote UART packet.
	 * <p/>
	 * {@code iSensePacket} contains a Remote UART packet if it is of type {@link ISensePacketType#PLOT} and if the first
	 * byte of its payload is one of {@link RUPPacket.Type#MESSAGE}, {@link RUPPacket.Type#SINK_REQUEST} or
	 * {@link RUPPacket.Type#SINK_RESPONSE}.
	 *
	 * @param packet the packet
	 *
	 * @return {@code true} if the packets type is a remote UART packet type, {@code false} otherwise
	 */
	public static boolean isRemoteUARTPacket(final ISensePacket packet) {
		return ISensePacketType.PLOT.equals(ISensePacketType.fromValue(packet.getType())) &&
				isRemoteUARTPacket(packet.getPayload().getByte(0));
	}

}
