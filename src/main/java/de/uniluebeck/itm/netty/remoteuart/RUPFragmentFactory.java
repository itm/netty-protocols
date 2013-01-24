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
package de.uniluebeck.itm.netty.remoteuart;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * A factory for creating or parsing remote UART packets.
 */
public class RUPFragmentFactory {

	/**
	 * Creates a new {@link RUPFragment} instance. Bytes will be copied from the source.
	 *
	 * @param cmdType
	 * 		the type of the packet
	 * @param sequenceNumber
	 * 		the packets sequence number
	 * @param destination
	 * 		the destination address
	 * @param source
	 * 		the source address
	 * @param payload
	 * 		the payload of the packet
	 *
	 * @return the newly created {@link RUPFragment} instance
	 */
	public static RUPFragment create(RUPPacket.Type cmdType, byte sequenceNumber, long destination, long source,
									 byte[] payload) {
		return new RUPFragmentImpl(cmdType.getValue(), sequenceNumber, destination, source, payload);
	}

	/**
	 * Creates a new {@link RUPFragment} instance. Bytes will be copied from the source.
	 *
	 * @param cmdType
	 * 		the type of the packet
	 * @param sequenceNumber
	 * 		the packets sequence number
	 * @param destination
	 * 		the destination address
	 * @param source
	 * 		the source address
	 * @param payload
	 * 		the payload of the packet
	 *
	 * @return the newly created {@link RUPFragment} instance
	 */
	public static RUPFragment create(byte cmdType, byte sequenceNumber, long destination, long source, byte[] payload) {
		return new RUPFragmentImpl(cmdType, sequenceNumber, destination, source, payload);
	}

	/**
	 * Creates a new {@link RUPFragment} instance. {@code payload} bytes will be wrapped.
	 *
	 * @param cmdType
	 * 		the type of the packet
	 * @param sequenceNumber
	 * 		the packets sequence number
	 * @param destination
	 * 		the destination address
	 * @param source
	 * 		the source address
	 * @param payload
	 * 		the payload of the packet
	 *
	 * @return the newly created {@link RUPFragment} instance
	 */
	public static RUPFragment create(byte cmdType, byte sequenceNumber, long destination, long source,
									 ChannelBuffer payload) {
		return new RUPFragmentImpl(cmdType, sequenceNumber, destination, source, payload);
	}

	/**
	 * Wraps a byte-array and exposes it's content as a {@link RUPFragment}.
	 *
	 * @param bytes
	 * 		the bytes to wrap
	 *
	 * @return a newly created {@link RUPFragment} instance
	 */
	public static RUPFragment wrap(byte[] bytes) {
		return wrap(ChannelBuffers.wrappedBuffer(bytes));
	}

	public static RUPFragment wrap(ChannelBuffer channelBuffer) {
		return new RUPFragmentImpl(channelBuffer);
	}

}
