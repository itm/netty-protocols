package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.base64.Base64Encoder;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Base64EncoderFactory implements HandlerFactory {

	@Override
	public String getName() {
		return "base64-encoder";
	}

	@Override
	public String getDescription() {
		return "Encodes a ChannelBuffer into a Base64-encoded ChannelBuffer. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Encoder.html.";
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		Boolean breakLines = Util.getBooleanFromProperties(properties, "breakLines");

		Base64Encoder encoder;
		if (breakLines != null) {
			encoder = new Base64Encoder(breakLines);
		} else {
			encoder = new Base64Encoder();
		}

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}
}
