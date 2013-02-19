package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;

public class ZlibDecoderFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), new ZlibDecoder()));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "Decompresses a ChannelBuffer using the deflate algorithm. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/compression/ZlibDecoder.html.";
	}

	@Override
	public String getName() {
		return "zlib-decoder";
	}
}
