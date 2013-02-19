package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.util.PropertiesHelper;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;

public class ObjectDecoderFactory implements HandlerFactory {

	private static final String MAX_OBJECT_SIZE = "maxObjectSize";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		final Multimap<String, String> properties = config.getProperties();
		final Integer maxObjectSize = PropertiesHelper.getIntFromProperties(properties, MAX_OBJECT_SIZE);

		final ObjectDecoder decoder = maxObjectSize == null ?
				new ObjectDecoder() :
				new ObjectDecoder(maxObjectSize);

		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), decoder));
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
