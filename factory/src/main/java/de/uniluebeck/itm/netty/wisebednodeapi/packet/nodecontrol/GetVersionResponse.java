package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class GetVersionResponse extends Response {
	public GetVersionResponse(final GetVersionRequest request) {
		super(request);
	}
}
