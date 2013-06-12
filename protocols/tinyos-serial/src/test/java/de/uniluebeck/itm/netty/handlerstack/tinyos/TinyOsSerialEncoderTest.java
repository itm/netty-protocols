package de.uniluebeck.itm.netty.handlerstack.tinyos;

import de.uniluebeck.itm.netty.handlerstack.util.NettyStringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
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
		assertEqualBuffers(ENCODED_PACKET_1, (ChannelBuffer) encoderTinyOs.encode(null, null, DECODED_PACKET_1));
	}

	@Test
	public void testPacket2() throws Exception {
		assertEqualBuffers(ENCODED_PACKET_2, (ChannelBuffer) encoderTinyOs.encode(null, null, DECODED_PACKET_2));
	}

	@Test
	public void testPacket3() throws Exception {
		assertEqualBuffers(ENCODED_PACKET_3, (ChannelBuffer) encoderTinyOs.encode(null, null, DECODED_PACKET_3));
	}

	@Test
	public void testPacket4() throws Exception {
		assertEqualBuffers(ENCODED_PACKET_4, (ChannelBuffer) encoderTinyOs.encode(null, null, DECODED_PACKET_4));
	}

	private void assertEqualBuffers(final ChannelBuffer expected, final ChannelBuffer actual) {
		assertEquals("\nExpected: " + NettyStringUtils.toHexString(expected) + "\nActual:   " + NettyStringUtils.toHexString(actual), expected, actual);
	}
}
