package de.uniluebeck.itm.nettyprotocols;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class NewlineEncoderTest {

	@Mock
	private ChannelHandlerContext ctx;

	@Mock
	private Channel channel;

	private NewlineEncoder encoder;

	@Before
	public void setUp() throws Exception {
		encoder = new NewlineEncoder();
	}

	@Test
	public void testIfNewlineAppendedToString() throws Exception {
		assertEquals("hello\n", encoder.encode(ctx, channel, "hello"));
	}

	@Test
	public void testIfNewlineNotAppendedIfAlreadyContainedLF() throws Exception {
		String encoded = (String) encoder.encode(ctx, channel, "hello\n");
		assertTrue(encoded.indexOf("\n") == encoded.lastIndexOf("\n"));
	}

	@Test
	public void testIfNewlineNotAppendedIfAlreadyContainedCRLF() throws Exception {
		String encoded = (String) encoder.encode(ctx, channel, "hello\r\n");
		assertTrue(encoded.indexOf("\r\n") == encoded.lastIndexOf("\r\n"));
	}

	@Test
	public void testIfNewlineNotAppendedIfAlreadyContainedInChannelBufferLF() throws Exception {
		ChannelBuffer encodedBuf = (ChannelBuffer) encoder.encode(ctx, channel, wrappedBuffer("hello\n".getBytes()));
		String encoded = encodedBuf.toString(CharsetUtil.UTF_8);
		assertTrue(encoded.indexOf("\n") == encoded.lastIndexOf("\n"));
	}

	@Test
	public void testIfNewlineNotAppendedIfAlreadyContainedInChannelBufferCRLF() throws Exception {
		ChannelBuffer encodedBuf = (ChannelBuffer) encoder.encode(ctx, channel, wrappedBuffer("hello\n".getBytes()));
		String encoded = encodedBuf.toString(CharsetUtil.UTF_8);
		assertTrue(encoded.indexOf("\r\n") == encoded.lastIndexOf("\r\n"));
	}

	@Test
	public void testIfNewlineAppendedToChannelBuffer() throws Exception {
		ChannelBuffer encoded = (ChannelBuffer) encoder.encode(ctx, channel, wrappedBuffer("hello".getBytes()));
		assertTrue(encoded.toString(CharsetUtil.UTF_8).endsWith("\n"));
	}
}
