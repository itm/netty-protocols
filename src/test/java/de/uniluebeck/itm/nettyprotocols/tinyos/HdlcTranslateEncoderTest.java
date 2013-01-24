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
package de.uniluebeck.itm.nettyprotocols.tinyos;

import com.google.common.primitives.Bytes;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

public class HdlcTranslateEncoderTest {

	@Test
	public void testEncoding() {

		byte[] payloadBytes = "hello, world".getBytes();

		byte[] encodedBytes = encode(payloadBytes);

		byte[] expectedBytes = Bytes.concat(
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE_ARRAY,
				payloadBytes,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE_ARRAY
		);

		assertArrayEquals(expectedBytes, encodedBytes);
	}

	@Test
	public void testOneDelimiterByteStuffed() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x03,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.FRAME_DELIMITER_BYTE ^ 0x20),
				0x03,
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	@Test
	public void testTwoDelimiterBytesStuffed() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x03,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.FRAME_DELIMITER_BYTE ^ 0x20),
				0x03,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.FRAME_DELIMITER_BYTE ^ 0x20),
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	@Test
	public void testOneEscapeByteStuffed() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				0x03,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.ESCAPE_BYTE ^ 0x20),
				0x03,
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	@Test
	public void testTwoEscapeBytesStuffed() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				0x03,
				HdlcTranslateConstants.ESCAPE_BYTE,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.ESCAPE_BYTE ^ 0x20),
				0x03,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.ESCAPE_BYTE ^ 0x20),
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	@Test
	public void testDelimiterAndEscapeBytesStuffedNextToEachOther() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x03,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.ESCAPE_BYTE ^ 0x20),
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.FRAME_DELIMITER_BYTE ^ 0x20),
				0x03,
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	@Test
	public void testDelimiterAndEscapeBytesStuffedApartFromEachOther() throws Exception {

		byte[] decodedPayload = new byte[]{
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				0x03,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x04
		};

		byte[] expectedEncodedPayload = new byte[]{
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE,
				0x01,
				0x02,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.ESCAPE_BYTE ^ 0x20),
				0x03,
				HdlcTranslateConstants.ESCAPE_BYTE,
				(HdlcTranslateConstants.FRAME_DELIMITER_BYTE ^ 0x20),
				0x04,
				HdlcTranslateConstants.FRAME_DELIMITER_BYTE
		};

		byte[] actualEncodedPayload = encode(decodedPayload);

		assertArrayEquals(expectedEncodedPayload, actualEncodedPayload);
	}

	private byte[] encode(final byte[] payloadBytes) {
		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(new HdlcTranslateEncoder());
		embedder.offer(ChannelBuffers.wrappedBuffer(payloadBytes));

		ChannelBuffer encodedBuffer = embedder.poll();
		byte[] encodedBytes = new byte[encodedBuffer.readableBytes()];
		encodedBuffer.readBytes(encodedBytes);
		return encodedBytes;
	}

}
