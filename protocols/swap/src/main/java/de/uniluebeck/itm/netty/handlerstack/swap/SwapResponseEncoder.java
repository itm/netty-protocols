package de.uniluebeck.itm.netty.handlerstack.swap;

import de.uniluebeck.itm.nettyrxtx.rup.RUPPacket;
import de.uniluebeck.itm.nettyrxtx.rup.RUPPacketImpl;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;


public class SwapResponseEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (!(msg instanceof SwapResponse)) {
			return msg;
		}

		SwapResponse response = (SwapResponse) msg;

		return new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				response.getDestination(),
				response.getSource(),
				response.getBuffer()
		);
	}
}
