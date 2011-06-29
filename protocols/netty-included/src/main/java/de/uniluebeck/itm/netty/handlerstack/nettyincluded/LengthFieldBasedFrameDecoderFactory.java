package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.FixedLengthFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import sun.tools.tree.NewArrayExpression;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LengthFieldBasedFrameDecoderFactory implements HandlerFactory {

	@Override
	public String getName() {
		return "length-field-based-frame-decoder";
	}

	@Override
	public String getDescription() {
		return "A decoder that splits the received ChannelBuffers dynamically by the value of the length field in "
				+ "the message. See http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/frame/LengthFieldBasedFrameDecoder.html.";
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		int maxFrameLength = Util.getIntFromProperties(properties, "maxFrameLength");
		int lengthFieldOffset = Util.getIntFromProperties(properties, "lengthFieldOffset");
		int lengthFieldLength = Util.getIntFromProperties(properties, "lengthFieldLength");

		Integer lengthAdjustment = Util.getIntFromProperties(properties, "lengthAdjustment");
		Integer initialBytesToStrip = Util.getIntFromProperties(properties, "initialBytesToStrip");

		boolean invalidOptionalProperties = (lengthAdjustment == null && initialBytesToStrip != null) ||
				(lengthAdjustment != null && initialBytesToStrip == null);

		if (invalidOptionalProperties) {
			throw new RuntimeException("The properties \"lengthAdjustment\" and \"initialBytesToStrip\" must both"
					+ " either be null or non-null.");
		}

		boolean optionalPropertiesProvided = lengthAdjustment != null && initialBytesToStrip != null;

		LengthFieldBasedFrameDecoder decoder;
		if (optionalPropertiesProvided) {
			decoder = new LengthFieldBasedFrameDecoder(
					maxFrameLength,
					lengthFieldOffset,
					lengthFieldLength,
					lengthAdjustment,
					initialBytesToStrip
			);
		} else {
			decoder = new LengthFieldBasedFrameDecoder(
					maxFrameLength,
					lengthFieldOffset,
					lengthFieldLength
			);
		}

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, decoder));
	}
}
