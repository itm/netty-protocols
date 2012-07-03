package de.uniluebeck.itm.netty.handlerstack.serialp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerialPEncoderTest implements SerialPTestConstants {

	private SerialPEncoder encoder;

	@Before
	public void setUp() throws Exception {
		encoder = new SerialPEncoder();
	}

	@After
	public void tearDown() throws Exception {
		encoder = null;
	}

	@Test
	public void testPacket1() throws Exception {
		assertEquals(ENCODED_PACKET_1, encoder.encode(null, null, DECODED_PACKET_1));
	}

	@Test
	public void testPacket2() throws Exception {
		assertEquals(ENCODED_PACKET_2, encoder.encode(null, null, DECODED_PACKET_2));
	}

	@Test
	public void testPacket3() throws Exception {
		assertEquals(ENCODED_PACKET_3, encoder.encode(null, null, DECODED_PACKET_3));
	}

	@Test
	public void testPacket4() throws Exception {
		assertEquals(ENCODED_PACKET_4, encoder.encode(null, null, DECODED_PACKET_4));
	}

}
