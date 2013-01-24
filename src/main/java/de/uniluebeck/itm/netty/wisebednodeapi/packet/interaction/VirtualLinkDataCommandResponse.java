package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:37
 * To change this template use File | Settings | File Templates.
 */
public class VirtualLinkDataCommandResponse extends CommandResponse {

	public VirtualLinkDataCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.VIRTUAL_LINK_DATA, requestID, result, payload);
	}
}
