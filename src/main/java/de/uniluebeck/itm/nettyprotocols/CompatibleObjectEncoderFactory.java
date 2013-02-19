package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.util.PropertiesHelper;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectEncoder;

public class CompatibleObjectEncoderFactory implements HandlerFactory {

	private static final String RESET_INTERVAL = "resetInterval";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		final Integer resetInterval = PropertiesHelper.getIntFromProperties(config.getProperties(), RESET_INTERVAL);

		final ChannelHandler encoder = resetInterval == null ?
				new CompatibleObjectEncoder() :
				new CompatibleObjectEncoder(resetInterval);

		return new NamedChannelHandlerList(new NamedChannelHandler(config.getInstanceName(), encoder));
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
