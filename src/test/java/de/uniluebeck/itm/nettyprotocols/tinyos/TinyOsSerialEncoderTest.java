package de.uniluebeck.itm.nettyprotocols.tinyos;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TinyOsSerialEncoderTest implements TinyOsSerialTestConstants {

	private TinyOsSerialEncoder encoderTinyOs;

	@Before
	public void setUp() throws Exception {
		encoderTinyOs = new TinyOsSerialEncoder();
	}

	@After
	public void tearDown() throws Exception {
		encoderTinyOs = null;
	}

	@Test
	public void testPacket1() throws Exception {
		assertEquals(ENCODED_PACKET_1, encoderTinyOs.encode(null, null, DECODED_PACKET_1));
	}

	@Test
	public void testPacket2() throws Exception {
		assertEquals(ENCODED_PACKET_2, encoderTinyOs.encode(null, null, DECODED_PACKET_2));
	}

	@Test
	public void testPacket3() throws Exception {
		assertEquals(ENCODED_PACKET_3, encoderTinyOs.encode(null, null, DECODED_PACKET_3));
	}

	@Test
	public void testPacket4() throws Exception {
		assertEquals(ENCODED_PACKET_4, encoderTinyOs.encode(null, null, DECODED_PACKET_4));
	}
}
