package de.uniluebeck.itm.nettyprotocols.isense.otap.program;

import de.uniluebeck.itm.nettyprotocols.util.HeaderAndJavaBeansXMLDecoderEncoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class ISenseOtapProgramResultUpstreamDecoder extends OneToOneDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}

		ISenseOtapProgramResult result =
				HeaderAndJavaBeansXMLDecoderEncoder.decode(ISenseOtapProgramResult.SERIALIZATION_HEADER,
						ISenseOtapProgramResult.class, (ChannelBuffer) msg
				);

		return result != null ? result : msg;
	}

}
