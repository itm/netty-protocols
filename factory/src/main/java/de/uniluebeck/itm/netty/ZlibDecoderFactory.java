package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ZlibDecoderFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, new ZlibDecoder()));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
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
