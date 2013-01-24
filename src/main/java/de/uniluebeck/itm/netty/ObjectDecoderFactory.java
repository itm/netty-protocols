package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ObjectDecoderFactory implements HandlerFactory {

	private static final String MAX_OBJECT_SIZE = "maxObjectSize";

	@Override
	@SuppressWarnings("unchecked")
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		final Integer maxObjectSize = Util.getIntFromProperties(properties, MAX_OBJECT_SIZE);

		final ObjectDecoder decoder = maxObjectSize == null ?
				new ObjectDecoder() :
				new ObjectDecoder(maxObjectSize);

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, decoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(MAX_OBJECT_SIZE, "(int, optional, default=1048576) the maximum byte length of the serialized object. "
				+ "if the length of the received object is greater than this value, StreamCorruptedException will be "
				+ "raised."
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "A decoder which deserializes the received ChannelBuffers into Java objects. Please note that the "
				+ "serialized form this decoder expects is not compatible with the standard ObjectOutputStream. "
				+ "Please use ObjectEncoder or ObjectEncoderOutputStream to ensure the interoperability with this "
				+ "decoder. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/serialization/ObjectDecoder.html.";
	}

	@Override
	public String getName() {
		return "object-decoder";
	}
}
