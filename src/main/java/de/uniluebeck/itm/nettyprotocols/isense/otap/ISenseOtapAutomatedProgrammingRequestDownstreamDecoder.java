package de.uniluebeck.itm.nettyprotocols.isense.otap;

import de.uniluebeck.itm.nettyprotocols.util.HeaderAndJavaBeansXMLDecoderEncoder;
import de.uniluebeck.itm.nettyprotocols.util.OneToOneDownstreamDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * This OneToOneEncoder "decodes" a byte array to an ISenseOtapAutomatedProgrammingRequest object. It is intended to be
 * used in a downstream filter pipeline which receives programming requests. The serialized object must contain the
 * String and then immediately afterwards the XML-serialized object (using
 * {@link java.beans.XMLEncoder}).
 */
public class ISenseOtapAutomatedProgrammingRequestDownstreamDecoder extends OneToOneDownstreamDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}

		ISenseOtapAutomatedProgrammingRequest result =
				HeaderAndJavaBeansXMLDecoderEncoder.decode(ISenseOtapAutomatedProgrammingRequest.SERIALIZATION_HEADER,
						ISenseOtapAutomatedProgrammingRequest.class, (ChannelBuffer) msg
				);

		return result != null ? result : msg;
	}
}
