package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class DisablePhysicalLinkCommandResponse extends CommandResponse {
	public DisablePhysicalLinkCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.LinkControl.DISABLE_PHYSICAL_LINK, requestID, result, payload);
	}
}
