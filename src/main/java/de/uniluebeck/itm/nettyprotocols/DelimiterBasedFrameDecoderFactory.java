package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

import java.nio.charset.Charset;

public class DelimiterBasedFrameDecoderFactory implements HandlerFactory {

	private static final String MAX_FRAME_LENGTH = "maxFrameLength";

	private static final String STRIP_DELIMITER = "stripDelimiter";

	private static final String FRAME_DELIMITERS = "frameDelimiters";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		final int maxFrameLength = Integer.parseInt(config.getProperties().get(MAX_FRAME_LENGTH).iterator().next());
		final boolean stripDelimiter = Boolean.parseBoolean(config.getProperties().get(STRIP_DELIMITER).iterator().next());
		final ChannelBuffer frameDelimiters = ChannelBuffers.copiedBuffer(
				config.getProperties().get(FRAME_DELIMITERS).iterator().next().toCharArray(),
				Charset.defaultCharset()
		);
		return new NamedChannelHandlerList(
				new NamedChannelHandler(config.getInstanceName(),
						new DelimiterBasedFrameDecoder(maxFrameLength, stripDelimiter, frameDelimiters)
				)
		);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(MAX_FRAME_LENGTH, "(int) the maximum length of the decoded frame. A TooLongFrameException is thrown if the length of the frame exceeds this value.");
		map.put(STRIP_DELIMITER, "(boolean) whether the decoded frame should strip out the delimiter or not");
		map.put(FRAME_DELIMITERS, "(String) the delimiter");
		return map;
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers by one or more delimiters. It is particularly "
				+ "useful for decoding the frames which ends with a delimiter such as NUL or newline characters. See "
				+ "http://docs.jboss.org/netty/3.2/api/index.html?org/jboss/netty/handler/codec/frame/DelimiterBasedFrameDecoder.html.";
	}

	@Override
	public String getName() {
		return "delimiter-based-frame-decoder";
	}
}
