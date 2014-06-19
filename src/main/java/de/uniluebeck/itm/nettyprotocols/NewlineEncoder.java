package de.uniluebeck.itm.nettyprotocols;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.util.CharsetUtil;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

public class NewlineEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg)
			throws Exception {
		if (msg instanceof String) {
			String bufString = (String) msg;
			if (bufString.endsWith("\n") || bufString.endsWith("\r\n")) {
				return bufString;
			} else {
				return bufString + '\n';
			}
		} else if (msg instanceof ChannelBuffer) {
			final ChannelBuffer buf = (ChannelBuffer) msg;
			final String bufString = buf.toString(CharsetUtil.UTF_8);
			if (bufString.endsWith("\n") || bufString.endsWith("\r\n")) {
				return buf;
			} else {
				return wrappedBuffer(buf, wrappedBuffer(new byte[]{'\n'}));
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
