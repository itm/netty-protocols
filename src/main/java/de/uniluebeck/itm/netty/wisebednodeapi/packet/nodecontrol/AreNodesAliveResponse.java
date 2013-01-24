package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:39
 * To change this template use File | Settings | File Templates.
 */
public class AreNodesAliveResponse extends Response {
	public AreNodesAliveResponse(final AreNodesAliveRequest request) {
		super(request);
	}
}
