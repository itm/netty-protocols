package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class StringEncoderFactory implements HandlerFactory {

	private static final String CHARSET_NAME = "charsetName";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		final Multimap<String, String> properties = config.getProperties();
		final String charsetName = properties.get(CHARSET_NAME).iterator().next();

		final StringEncoder encoder = charsetName == null ?
				new StringEncoder(CharsetUtil.UTF_8) :
				new StringEncoder(Charset.forName(charsetName));

		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), encoder));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(CHARSET_NAME, "(int, optional, default=UTF-8) the character set to use (e.g. UTF-16, UTF-16BE, "
				+ "UTF-16LE, UTF-8, ISO-8859-1, US-ASCII)"
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "Encodes the requested String into a ChannelBuffer. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/string/StringEncoder.html.";
	}

	@Override
	public String getName() {
		return "string-encoder";
	}
}
