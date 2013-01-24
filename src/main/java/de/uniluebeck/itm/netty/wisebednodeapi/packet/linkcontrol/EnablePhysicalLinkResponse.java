package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */
public class EnablePhysicalLinkResponse extends Response {
	public EnablePhysicalLinkResponse(final EnablePhysicalLinkRequest request) {
		super(request);
	}

	public long getNodeB(){
		return ((EnablePhysicalLinkRequest) request).getNodeB();
	}
}
