package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class NewlineEncoderFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), new NewlineEncoder()));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "An encoder that adds a newline character (\\n) to every received packet if it is not already present "
				+ "(\\r\\n or \\n). Typically used for ASCII-based text protocols and in Shell-like interactive "
				+ "environments (such as in Contiki).";
	}

	@Override
	public String getName() {
		return "newline-encoder";
	}
}
