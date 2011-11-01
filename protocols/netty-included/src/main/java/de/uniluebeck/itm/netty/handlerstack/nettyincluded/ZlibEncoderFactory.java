package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.compression.ZlibEncoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static de.uniluebeck.itm.netty.handlerstack.nettyincluded.Util.getIntFromProperties;

public class ZlibEncoderFactory implements HandlerFactory {

	private static final String COMPRESSION_LEVEL = "compressionLevel";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		final Integer compressionLevel = getIntFromProperties(properties, COMPRESSION_LEVEL);

		final ZlibEncoder encoder = compressionLevel == null ?
				new ZlibEncoder() :
				new ZlibEncoder(compressionLevel);

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
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
