package de.uniluebeck.itm.nettyprotocols.tinyos;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TinyOsSerialDecoderTest implements TinyOsSerialTestConstants {

	private TinyOsSerialDecoder decoderTinyOs;

	@Before
	public void setUp() throws Exception {
		decoderTinyOs = new TinyOsSerialDecoder();
	}

	@After
	public void tearDown() throws Exception {
		decoderTinyOs = null;
	}

	@Test
	public void testPacket1() throws Exception {
		assertEquals(DECODED_PACKET_1, decoderTinyOs.decode(null, null, ENCODED_PACKET_1));
	}

	@Test
	public void testPacket2() throws Exception {
		assertEquals(DECODED_PACKET_2, decoderTinyOs.decode(null, null, ENCODED_PACKET_2));
	}

	@Test
	public void testPacket3() throws Exception {
		assertEquals(DECODED_PACKET_3, decoderTinyOs.decode(null, null, ENCODED_PACKET_3));
	}

	@Test
	public void testPacket4() throws Exception {
		assertEquals(DECODED_PACKET_4, decoderTinyOs.decode(null, null, ENCODED_PACKET_4));
	}

	@Test
	public void testCrcInvalidPacket1() throws Exception {
		assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_1));
	}

	@Test
	public void testCrcInvalidPacket2() throws Exception {
		assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_2));
	}

	@Test
	public void testCrcInvalidPacket3() throws Exception {
		assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_3));
	}

	@Test
	public void testCrcInvalidPacket4() throws Exception {
		assertNull(decoderTinyOs.decode(null, null, CRC_INVALID_ENCODED_PACKET_4));
	}
}
