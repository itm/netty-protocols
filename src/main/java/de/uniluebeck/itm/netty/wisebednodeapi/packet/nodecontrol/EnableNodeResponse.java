package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class EnableNodeResponse extends Response {
	public EnableNodeResponse(final EnableNodeRequest request) {
		super(request);
	}
}
