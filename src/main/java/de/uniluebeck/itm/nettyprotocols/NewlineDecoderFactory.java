package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.util.PropertiesHelper;

public class NewlineDecoderFactory implements HandlerFactory {

	private static final String MAX_FRAME_LENGTH = "maxFrameLength";

	private static final String STRIP_NEWLINE = "stripNewline";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		final Multimap<String, String> properties = config.getProperties();
		final Integer maxFrameLength = PropertiesHelper.getIntFromProperties(properties, MAX_FRAME_LENGTH);
		final Boolean stripNewline = PropertiesHelper.getBooleanFromProperties(properties, STRIP_NEWLINE);
		final NewlineDecoder decoder = new NewlineDecoder(maxFrameLength, stripNewline);
		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), decoder));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(MAX_FRAME_LENGTH, "maximum length of a message (i.e. maximum amount of characters (=bytes) between "
						+ "newline characters. If exceeded the data stream the message will be discarded with an error."
		);
		map.put(STRIP_NEWLINE,
				"set to 'true' if newline characters should be stripped out of the stream, 'false' if not"
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "An encoder that splits up a byte stream at every newline character (\\r\\n or \\n, respectively). "
				+ "Typically used for ASCII-based text protocols and in Shell-like interactive environments (such as in "
				+ "Contiki)";
	}

	@Override
	public String getName() {
		return "newline-decoder";
	}
}
