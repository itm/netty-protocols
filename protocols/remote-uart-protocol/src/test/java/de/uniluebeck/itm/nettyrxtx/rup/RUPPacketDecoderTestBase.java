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

import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import org.jboss.netty.buffer.ChannelBuffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;


public abstract class RUPPacketDecoderTestBase {

	private Random random;

	private Map<Long, Byte> lastSequenceNumbers;

	protected void setUp() {
		random = new Random();
		lastSequenceNumbers = Maps.newHashMap();
	}

	protected void tearDown() {
		random = null;
		lastSequenceNumbers = null;
	}

	protected ChannelBuffer createOpeningAndClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + 2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected ChannelBuffer createOpeningMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected ChannelBuffer createMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, payload.getBytes()).getChannelBuffer();
	}

	protected ChannelBuffer createClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected byte getRandomSequenceNumber(long sender) {
		byte sequenceNumber = (byte) (0xFF & (random.nextInt(255) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}

	protected byte getSubsequentSequenceNumber(long sender) {
		if (!lastSequenceNumbers.containsKey(sender)) {
			throw new IllegalArgumentException("No first sequence number existing!");
		}
		byte lastSequenceNumber = lastSequenceNumbers.get(sender);
		byte sequenceNumber = (byte) (0xFF & ((lastSequenceNumber + 1) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}
}
