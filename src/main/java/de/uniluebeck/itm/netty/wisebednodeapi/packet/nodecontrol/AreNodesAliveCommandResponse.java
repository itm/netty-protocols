package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:04
 * To change this template use File | Settings | File Templates.
 */
public class AreNodesAliveCommandResponse extends CommandResponse {
	public AreNodesAliveCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.ARE_NODES_ALIVE, requestID, result, payload);
	}
}
