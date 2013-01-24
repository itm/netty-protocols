package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */
public class EnablePhysicalLinkCommandResponse extends CommandResponse {

	public EnablePhysicalLinkCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.LinkControl.ENABLE_PHYSICAL_LINK, requestID, result, payload);
	}
}
