package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 17:29
 * To change this template use File | Settings | File Templates.
 */
public class DestroyVirtualLinkCommandResponse extends CommandResponse {

	public DestroyVirtualLinkCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.LinkControl.DESTROY_VIRTUAL_LINK, requestID, result, payload);
	}
}
