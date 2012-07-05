package de.uniluebeck.itm.netty.handlerstack.serialp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SerialPDecoderTest implements SerialPTestConstants {

	private SerialPDecoder decoder;

	@Before
	public void setUp() throws Exception {
		decoder = new SerialPDecoder();
	}

	@After
	public void tearDown() throws Exception {
		decoder = null;
	}

	@Test
	public void testPacket1() throws Exception {
		assertEquals(DECODED_PACKET_1, decoder.decode(null, null, ENCODED_PACKET_1));
	}

	@Test
	public void testPacket2() throws Exception {
		assertEquals(DECODED_PACKET_2, decoder.decode(null, null, ENCODED_PACKET_2));
	}

	@Test
	public void testPacket3() throws Exception {
		assertEquals(DECODED_PACKET_3, decoder.decode(null, null, ENCODED_PACKET_3));
	}

	@Test
	public void testPacket4() throws Exception {
		assertEquals(DECODED_PACKET_4, decoder.decode(null, null, ENCODED_PACKET_4));
	}

	@Test
	public void testCrcInvalidPacket1() throws Exception {
		assertNull(decoder.decode(null, null, CRC_INVALID_ENCODED_PACKET_1));
	}

	@Test
	public void testCrcInvalidPacket2() throws Exception {
		assertNull(decoder.decode(null, null, CRC_INVALID_ENCODED_PACKET_2));
	}

	@Test
	public void testCrcInvalidPacket3() throws Exception {
		assertNull(decoder.decode(null, null, CRC_INVALID_ENCODED_PACKET_3));
	}

	@Test
	public void testCrcInvalidPacket4() throws Exception {
		assertNull(decoder.decode(null, null, CRC_INVALID_ENCODED_PACKET_4));
	}
}
