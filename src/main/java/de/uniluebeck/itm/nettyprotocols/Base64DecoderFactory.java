package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.base64.Base64Decoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Base64DecoderFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, new Base64Decoder()));
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
		return "Decodes a Base64-encoded ChannelBuffer or US-ASCII String into a ChannelBuffer. See"
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Decoder.html.";
	}

	@Override
	public String getName() {
		return "base64-decoder";
	}
}
