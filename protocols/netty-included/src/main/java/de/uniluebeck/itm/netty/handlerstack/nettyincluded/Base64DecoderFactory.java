package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.base64.Base64Encoder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Base64DecoderFactory implements HandlerFactory {

	@Override
	public String getName() {
		return "base64-decoder";
	}

	@Override
	public String getDescription() {
		return "Decodes a Base64-encoded ChannelBuffer or US-ASCII String into a ChannelBuffer. See"
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Decoder.html.";
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, new Base64Decoder()));
	}
}
