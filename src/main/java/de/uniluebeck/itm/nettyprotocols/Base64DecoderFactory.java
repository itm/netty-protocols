package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.base64.Base64Decoder;

public class Base64DecoderFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), new Base64Decoder()));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "Decodes a Base64-encoded ChannelBuffer or US-ASCII String into a ChannelBuffer. See"
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Decoder.html.";
	}

	@Override
	public String getName() {
		return "base64-decoder";
	}
}
