package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIDResponse extends Response{
	public SetVirtualIDResponse(final SetVirtualIDRequest request) {
		super(request);
	}

	public long getVirtualNodeId(){
		return ((SetVirtualIDRequest) request).getVirtualNodeId();
	}
}
