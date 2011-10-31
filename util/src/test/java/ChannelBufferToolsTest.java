import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import static de.uniluebeck.itm.netty.handlerstack.util.ChannelBufferTools.toPrintableString;
import static org.junit.Assert.assertEquals;

public class ChannelBufferToolsTest {

	@Test
	public void testToPrintableStringWithEmptyBuffer() throws Exception {

		final String expected = "";
		final String actual = toPrintableString(ChannelBuffers.buffer(0), 10);

		assertEquals(expected, actual);
	}

	@Test
	public void testToPrintableStringWithBufferSmallerThanMaxLength() throws Exception {

		final String expected = "0123456789";
		final String actual = toPrintableString(
				ChannelBuffers.copiedBuffer(expected.getBytes()),
				expected.getBytes().length + 1
		);

		assertEquals(expected, actual);
	}

	@Test
	public void testToPrintableStringWithBufferLargerThanMaxLength() throws Exception {

		final String original = "0123456789";
		final String expected = "012345";
		final String actual = toPrintableString(
				ChannelBuffers.copiedBuffer(original.getBytes()),
				expected.getBytes().length
		);

		assertEquals(expected, actual);

	}
}
