package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.compression.ZlibEncoder;

import static de.uniluebeck.itm.nettyprotocols.util.PropertiesHelper.getIntFromProperties;

public class ZlibEncoderFactory implements HandlerFactory {

	private static final String COMPRESSION_LEVEL = "compressionLevel";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		final Integer compressionLevel = getIntFromProperties(config.getProperties(), COMPRESSION_LEVEL);

		final ZlibEncoder encoder = compressionLevel == null ?
				new ZlibEncoder() :
				new ZlibEncoder(compressionLevel);

		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), encoder));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(COMPRESSION_LEVEL, "(int, optional, default=6) 1 yields the fastest compression and 9 yields the best "
				+ "compression. 0 means no compression. The default compression level is 6."
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "Compresses a ChannelBuffer using the deflate algorithm. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/compression/ZlibEncoder.html.";
	}

	@Override
	public String getName() {
		return "zlib-encoder";
	}
}
