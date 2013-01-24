package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ObjectEncoderFactory implements HandlerFactory {

	private static final String ESTIMATED_LENGTH = "estimatedLength";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		final Integer estimatedLength = Util.getIntFromProperties(properties, ESTIMATED_LENGTH);

		final ObjectEncoder encoder = estimatedLength == null ?
				new ObjectEncoder() :
				new ObjectEncoder(estimatedLength);

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(ESTIMATED_LENGTH, "(int, optional, default=512) the estimated byte length of the serialized form of an "
				+ "object. If the length of the serialized form exceeds this value, the internal buffer will be "
				+ "expanded automatically at the cost of memory bandwidth. If this value is too big, it will also "
				+ "waste memory bandwidth. To avoid unnecessary memory copy or allocation cost, please specify the "
				+ "properly estimated value."
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "An encoder which serializes a Java object into a ChannelBuffer. Please note that the serialized form "
				+ "this encoder produces is not compatible with the standard ObjectInputStream. Please use "
				+ "ObjectDecoder or ObjectDecoderInputStream to ensure the interoperability with this encoder.See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/serialization/ObjectEncoder.html.";
	}

	@Override
	public String getName() {
		return "object-encoder";
	}
}
