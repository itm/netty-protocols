package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectEncoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class CompatibleObjectEncoderFactory implements HandlerFactory {

	private static final String RESET_INTERVAL = "resetInterval";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties)
			throws Exception {

		final Integer resetInterval = Util.getIntFromProperties(properties, RESET_INTERVAL);

		final ChannelHandler encoder = resetInterval == null ?
				new CompatibleObjectEncoder() :
				new CompatibleObjectEncoder(resetInterval);

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(RESET_INTERVAL, "(int, optional, default=16) the number of objects between ObjectOutputStream.reset(). "
				+ "0 will disable resetting the stream, but the remote peer will be at the risk of getting "
				+ "OutOfMemoryError in the long term."
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "An encoder which serializes a Java object into a ChannelBuffer (interoperability version). This "
				+ "encoder is interoperable with the standard Java object streams such as ObjectInputStream and "
				+ "ObjectOutputStream. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/serialization/CompatibleObjectEncoder.html.";
	}

	@Override
	public String getName() {
		return "compatible-object-encoder";
	}
}
