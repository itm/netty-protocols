package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FixedLengthFrameDecoderFactory implements HandlerFactory {

	@Override
	public String getName() {
		return "fixed-length-frame-decoder";
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers into a fixed number of bytes. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/FixedLengthFrameDecoder.html.";
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int frameLength = Integer.parseInt(properties.get("frameLength").iterator().next());
		return newArrayList(
				new Tuple<String, ChannelHandler>(
						instanceName,
						new FixedLengthFrameDecoder(frameLength)
				)
		);
	}
}
