package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public class DisablePhysicalLinkResponse extends Response{
	public DisablePhysicalLinkResponse(final DisablePhysicalLinkRequest request) {
		super(request);
	}

	public long getNodeB(){
		return ((DisablePhysicalLinkRequest) request).getNodeB();
	}
}
