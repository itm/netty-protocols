package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FixedLengthFrameDecoderFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int frameLength = Integer.parseInt(properties.get("frameLength").iterator().next());
		return newArrayList(
				new Tuple<String, ChannelHandler>(
						instanceName,
						new FixedLengthFrameDecoder(frameLength)
				)
		);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put("frameLength", "(int) the number of bytes of an individual frame");
		return map;
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers into a fixed number of bytes. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/FixedLengthFrameDecoder.html.";
	}

	@Override
	public String getName() {
		return "fixed-length-frame-decoder";
	}
}
