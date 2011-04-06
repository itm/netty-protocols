package de.uniluebeck.itm.netty.handlerstack.swap;

import de.uniluebeck.itm.nettyrxtx.rup.RUPPacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;


public class SwapRequestDecoder extends OneToOneDecoder {
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		if (!(msg instanceof RUPPacket)) {
			return msg;
		}

		RUPPacket packet = (RUPPacket) msg;

		if (RUPPacket.Type.MESSAGE.getValue() != packet.getCmdType()) {
			return msg;
		}

		return SwapRequest.Factory.wrap(packet.getDestination(), packet.getSource(), packet.getPayload());
	}
}
