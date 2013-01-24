package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */
public class ResetNodeResponse extends Response {
	public ResetNodeResponse(final ResetNodeRequest request) {
		super(request);
	}

	public short getTime() {
		return ((ResetNodeRequest) request).getTime();
	}
}
