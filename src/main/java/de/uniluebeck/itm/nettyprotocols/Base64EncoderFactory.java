package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.util.PropertiesHelper;
import org.jboss.netty.handler.codec.base64.Base64Encoder;

public class Base64EncoderFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		Boolean breakLines = PropertiesHelper.getBooleanFromProperties(config.getProperties(), "breakLines");

		Base64Encoder encoder;
		if (breakLines != null) {
			encoder = new Base64Encoder(breakLines);
		} else {
			encoder = new Base64Encoder();
		}

		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), encoder));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> configurationOptions = HashMultimap.create();
		configurationOptions.put("breakLines", "(boolean)");
		return configurationOptions;
	}

	@Override
	public String getDescription() {
		return "Encodes a ChannelBuffer into a Base64-encoded ChannelBuffer. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Encoder.html.";
	}

	@Override
	public String getName() {
		return "base64-encoder";
	}
}
