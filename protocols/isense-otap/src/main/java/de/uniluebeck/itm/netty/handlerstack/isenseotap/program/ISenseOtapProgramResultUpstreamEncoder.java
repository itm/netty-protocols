package de.uniluebeck.itm.netty.handlerstack.isenseotap.program;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import de.uniluebeck.itm.netty.handlerstack.util.HeaderAndJavaBeansXMLDecoderEncoder;
import de.uniluebeck.itm.netty.handlerstack.util.OneToOneUpstreamEncoder;

public class ISenseOtapProgramResultUpstreamEncoder extends OneToOneUpstreamEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof ISenseOtapProgramResult))
            return msg;

        return HeaderAndJavaBeansXMLDecoderEncoder.encode(ISenseOtapProgramResult.SERIALIZATION_HEADER, msg);
    }

}
