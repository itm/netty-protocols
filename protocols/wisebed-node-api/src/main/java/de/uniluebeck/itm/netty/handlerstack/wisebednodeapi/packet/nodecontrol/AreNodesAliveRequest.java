package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class AreNodesAliveRequest extends Request{
	public AreNodesAliveRequest(final byte requestID, final byte[] payload) {
		super(CommandType.NodeControl.ARE_NODES_ALIVE, requestID, payload);
	}
}
