package de.uniluebeck.itm.netty.isense.otap;

import de.uniluebeck.itm.netty.util.HeaderAndJavaBeansXMLDecoderEncoder;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class ISenseOtapAutomatedProgrammingRequestDownstreamEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof ISenseOtapAutomatedProgrammingRequest))
            return msg;

        return HeaderAndJavaBeansXMLDecoderEncoder.encode(ISenseOtapAutomatedProgrammingRequest.SERIALIZATION_HEADER,
				msg
		);
    }

}
