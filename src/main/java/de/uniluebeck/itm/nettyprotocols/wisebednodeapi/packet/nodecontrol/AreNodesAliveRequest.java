package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class AreNodesAliveRequest extends Request {

	public AreNodesAliveRequest(final byte requestID, final byte[] payload) {
		super(CommandType.NodeControl.ARE_NODES_ALIVE, requestID, payload);
	}
}
