package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;

public class FixedLengthFrameDecoderFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		int frameLength = Integer.parseInt(config.getProperties().get("frameLength").iterator().next());
		return new NamedChannelHandlerList(
				new NamedChannelHandler(config.getInstanceName(), new FixedLengthFrameDecoder(frameLength))
		);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put("frameLength", "(int) the number of bytes of an individual frame");
		return map;
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers into a fixed number of bytes. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/FixedLengthFrameDecoder.html.";
	}

	@Override
	public String getName() {
		return "fixed-length-frame-decoder";
	}
}
