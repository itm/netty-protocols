package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.06.11
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualLinkResponse extends Response {

	public SetVirtualLinkResponse(final SetVirtualLinkRequest setVirtualLinkRequest) {
		super(setVirtualLinkRequest);
	}

	public long getDestinationNode() {
		return ((SetVirtualLinkRequest) request).getDestinationNode();
	}

}
