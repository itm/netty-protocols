package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualLinkCommandResponse extends CommandResponse {

	public SetVirtualLinkCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.LinkControl.SET_VIRTUAL_LINK, requestID, result, payload);
	}
}
