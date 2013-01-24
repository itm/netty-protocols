package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputVirtualLinkCommandResponse extends CommandResponse {

	public NodeOutputVirtualLinkCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.NODE_OUTPUT_VIRTUAL_LINK, requestID, result, payload);
	}
}
