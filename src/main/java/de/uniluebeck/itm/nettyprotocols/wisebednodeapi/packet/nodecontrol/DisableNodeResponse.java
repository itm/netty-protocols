package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class DisableNodeResponse extends Response {

	public DisableNodeResponse(final DisableNodeRequest request) {
		super(request);
	}
}
