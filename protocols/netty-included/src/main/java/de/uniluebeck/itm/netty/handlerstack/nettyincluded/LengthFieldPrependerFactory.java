package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LengthFieldPrependerFactory implements HandlerFactory {

	@Override
	public String getName() {
		return "length-field-prepender";
	}

	@Override
	public String getDescription() {
		return "An encoder that prepends the length of the message. The length value is prepended as a binary form. "
				+ "It is encoded in either big endian or little endian depending on the default ByteOrder of the "
				+ "current ChannelBufferFactory. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/LengthFieldPrepender.html";
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int lengthFieldLength = Util.getIntFromProperties(properties, "lengthFieldLength");
		Boolean lengthIncludesLengthFieldLength = Util.getBooleanFromProperties(
				properties,
				"lengthIncludesLengthFieldLength"
		);

		boolean optionalPropertiesProvided = lengthIncludesLengthFieldLength != null;

		LengthFieldPrepender prepender;
		if (optionalPropertiesProvided) {
			prepender = new LengthFieldPrepender(lengthFieldLength, lengthIncludesLengthFieldLength);
		} else {
			prepender = new LengthFieldPrepender(lengthFieldLength);
		}

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, prepender));
	}
}
