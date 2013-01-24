package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public class BinaryDataCommandResponse extends CommandResponse {

	public BinaryDataCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.BINARY_DATA, requestID, result, payload);
	}
}
