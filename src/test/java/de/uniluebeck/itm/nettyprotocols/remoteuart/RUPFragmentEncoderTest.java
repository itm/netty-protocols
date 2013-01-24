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
package de.uniluebeck.itm.nettyprotocols.remoteuart;

import de.uniluebeck.itm.nettyprotocols.isense.ISensePacket;
import de.uniluebeck.itm.nettyprotocols.isense.ISensePacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RUPFragmentEncoderTest {

	private EncoderEmbedder<ISensePacket> encoder;

	@Before
	public void setUp() {
		encoder = new EncoderEmbedder<ISensePacket>(new RUPFragmentEncoder());
	}

	@After
	public void tearDown() {
		encoder = null;
	}

	@Test
	public void testSingleFragment() {
		RUPFragment fragment = RUPFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 0,
				0x1234,
				0x4321,
				new byte[]{1, 2, 3}
		);
		encoder.offer(fragment);
		ISensePacket iSensePacket = encoder.poll();
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 0, 0x1234, 0x4321, new byte[] {1,2,3}, iSensePacket);
	}

	private void assertBufferCorrect(byte cmdType, byte sequenceNumber, long destination, long source, byte[] payload, ISensePacket iSensePacket) {
		Assert.assertEquals(ISensePacketType.CUSTOM_IN_1.getValue(), iSensePacket.getType());
		ChannelBuffer encodedPayload = iSensePacket.getPayload();
		assertEquals(cmdType, encodedPayload.getByte(0));
		assertEquals(sequenceNumber, encodedPayload.getByte(1));
		assertEquals(destination, encodedPayload.getLong(2));
		assertEquals(source, encodedPayload.getLong(10));
		assertEquals(payload.length, encodedPayload.getByte(18));
		byte[] encodedPayloadBytes = new byte[encodedPayload.readableBytes()-19];
		encodedPayload.getBytes(19, encodedPayloadBytes);
		assertArrayEquals(payload, encodedPayloadBytes);
	}

	@Test
	public void testMultipleFragments() {
		RUPFragment fragment1 = RUPFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 0,
				0x1234,
				0x4321,
				new byte[]{1, 2, 3}
		);
		RUPFragment fragment2 = RUPFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 1,
				0x2345,
				0x5432,
				new byte[]{3, 2, 1}
		);
		encoder.offer(fragment1);
		encoder.offer(fragment2);
		ISensePacket iSensePacket1 = encoder.poll();
		ISensePacket iSensePacket2 = encoder.poll();
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 0, 0x1234, 0x4321, new byte[] {1,2,3}, iSensePacket1);
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 1, 0x2345, 0x5432, new byte[] {3,2,1}, iSensePacket2);
	}

}
