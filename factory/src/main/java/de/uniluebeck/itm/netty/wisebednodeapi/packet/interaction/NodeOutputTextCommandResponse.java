package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputTextCommandResponse extends CommandResponse {
	public NodeOutputTextCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.NODE_OUTPUT_TEXT, requestID, result, payload);
	}
}
