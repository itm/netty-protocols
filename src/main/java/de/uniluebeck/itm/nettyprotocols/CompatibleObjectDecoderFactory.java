package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.netty.handler.codec.serialization.CompatibleObjectDecoder;

public class CompatibleObjectDecoderFactory implements HandlerFactory {

	private static final String RESET_INTERVAL = "resetInterval";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList(
				new NamedChannelHandler(config.getInstanceName(), new CompatibleObjectDecoder())
		);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "A decoder which deserializes the received ChannelBuffers into Java objects (interoperability version)."
				+ "This decoder is interoperable with the standard Java object streams such as ObjectInputStream and"
				+ "ObjectOutputStream. However, this decoder might perform worse than ObjectDecoder if the serialized "
				+ "object is big and complex. Also, it does not limit the maximum size of the object, and consequently "
				+ "your application might face the risk of DoS attack. Please use ObjectEncoder and ObjectDecoder if "
				+ "you are not required to keep the interoperability with the standard object streams."
				+ "Deprecated. This decoder has a known critical bug which fails to decode and raises a random "
				+ "exception in some circumstances. Avoid to use it whenever you can. The only workaround is to "
				+ "replace CompatibleObjectEncoder, CompatibleObjectDecoder, ObjectInputStream, and ObjectOutputStream "
				+ "with ObjectEncoder, ObjectDecoder, ObjectEncoderOutputStream, and ObjectDecoderInputStream "
				+ "respectively. This workaround requires both a client and a server to be modified. See"
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/serialization/CompatibleObjectDecoder.html.";
	}

	@Override
	public String getName() {
		return "compatible-object-decoder";
	}
}
