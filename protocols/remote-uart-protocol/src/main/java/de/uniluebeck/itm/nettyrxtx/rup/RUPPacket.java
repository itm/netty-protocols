/**
 * Copyright (c) 2010, Dennis Pfisterer, Institute of Telematics, University of Luebeck
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

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Interface that represents a Remote UART packet.
 */
public interface RUPPacket {

	public enum Type {

		/**
		 * A remote UART message.
		 */
		MESSAGE((byte) (0xFF & 60)),

		/**
		 * A remote UART message indicating that a node request a sink to send its output to.
		 */
		SINK_REQUEST((byte) (0xFF & 61)),

		/**
		 * A remote UART message indicating that the sender of this message offers to be a sink for the requester.
		 */
		SINK_RESPONSE((byte) (0xFF & 62)),

		/**
		 * A remote UART message that can be sent to the connected node to tell him to be sink for other nodes.
		 */
		SET_SINK((byte) (0xFF & 63));

		private final byte value;

		private Type(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		/**
		 * Returns the enum constant with value {@code value} or {@code null} if no RUPPacketType exists with value {@code
		 * value}.
		 *
		 * @param value the byte value of the packet type
		 *
		 * @return an RUPPacketType enum constant or {@code null} if {@code value} does not match
		 */
		public static Type fromValue(byte value) {
			switch (value) {
				case 60:
					return MESSAGE;
				case 61:
					return SINK_REQUEST;
				case 62:
					return SINK_RESPONSE;
				case 63:
					return SET_SINK;
				default:
					return null;
			}
		}
	}

	/**
	 * Returns the packets type.
	 *
	 * @return the packets type
	 */
	byte getCmdType();

	/**
	 * Returns the destination address.
	 *
	 * @return the destination address
	 */
	long getDestination();

	/**
	 * Returns the source address.
	 *
	 * @return the source address
	 */
	long getSource();

	/**
	 * Returns the payload of the packet. <b>Attention:</b> If this is an instance of RUPPacket the returned ChannelBuffer
	 * is the original buffer, not a copy. If this is an instanceof RUPFragment the returned ChannelBuffer is a slice
	 * of the backing ChannelBuffer object. Any changes to the returned buffer will be reflected by the packets buffer.
	 *
	 * @return the payload
	 */
	ChannelBuffer getPayload();

}
