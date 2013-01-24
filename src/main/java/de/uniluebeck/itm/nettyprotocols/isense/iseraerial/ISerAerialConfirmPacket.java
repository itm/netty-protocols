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
package de.uniluebeck.itm.nettyprotocols.isense.iseraerial;

import org.jboss.netty.buffer.ChannelBuffer;

public class ISerAerialConfirmPacket {

	private static final int HEADER_LENGTH = 3;

	public static final byte TYPE_CODE = 0x01;

	private final ChannelBuffer buffer;

	/**
	 * Creates a new ISerAerialConfirmPacket instance using {@code buffer} as its backing buffer.
	 *
	 * @param buffer
	 * 		the backing buffer
	 */
	public ISerAerialConfirmPacket(ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() != HEADER_LENGTH) {
			throw new Exception(String.format("Packet size of %d is wrong, expecting %d", buffer.readableBytes(),
					HEADER_LENGTH
			)
			);
		}

		if (buffer.getByte(0) != TYPE_CODE) {
			throw new Exception(String.format("Unexpected first type byte %d, expected %d", buffer.getByte(0),
					TYPE_CODE
			)
			);
		}

		this.buffer = buffer;
	}

	public int getState() {
		return 0xFF & buffer.getByte(1);
	}

	public int getTries() {
		return 0xFF & buffer.getByte(2);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ISerAerialConfirmPacket [getState()=");
		builder.append(getState());
		builder.append(", getTries()=");
		builder.append(getTries());
		builder.append("]");
		return builder.toString();
	}

}
