package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LengthFieldPrependerFactory implements HandlerFactory {

	private static final String LENGTH_FIELD_LENGTH = "lengthFieldLength";

	private static final String LENGTH_INCLUDES_LENGTH_FIELD_LENGTH = "lengthIncludesLengthFieldLength";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int lengthFieldLength = Util.getIntFromProperties(properties, LENGTH_FIELD_LENGTH);
		Boolean lengthIncludesLengthFieldLength = Util.getBooleanFromProperties(
				properties,
				LENGTH_INCLUDES_LENGTH_FIELD_LENGTH
		);

		boolean optionalPropertiesProvided = lengthIncludesLengthFieldLength != null;

		LengthFieldPrepender prepender = optionalPropertiesProvided ?
				new LengthFieldPrepender(lengthFieldLength, lengthIncludesLengthFieldLength) :
				new LengthFieldPrepender(lengthFieldLength);

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, prepender));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(LENGTH_FIELD_LENGTH,
				"(int) the length of the prepended length field. Only 1, 2, 3, 4, and 8 are allowed."
		);
		map.put(LENGTH_INCLUDES_LENGTH_FIELD_LENGTH,
				"(boolean, optional, default=false) if true, the length of the prepended length field is added to the "
						+ "value of the prepended length field."
		);
		return map;
	}

	@Override
	public String getDescription() {
		return "An encoder that prepends the length of the message. The length value is prepended as a binary form. "
				+ "It is encoded in either big endian or little endian depending on the default ByteOrder of the "
				+ "current ChannelBufferFactory. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/LengthFieldPrepender.html";
	}

	@Override
	public String getName() {
		return "length-field-prepender";
	}
}
