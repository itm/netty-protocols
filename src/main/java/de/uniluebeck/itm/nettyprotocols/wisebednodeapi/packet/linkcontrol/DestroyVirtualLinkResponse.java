package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class DestroyVirtualLinkResponse extends Response {

	public DestroyVirtualLinkResponse(final DestroyVirtualLinkRequest request) {
		super(request);
	}

	public long getDestinationNode() {
		return ((DestroyVirtualLinkRequest) request).getDestinationNode();
	}

}
