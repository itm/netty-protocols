package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIdResponse extends Response{
	public SetVirtualIdResponse(final SetVirtualIdRequest request) {
		super(request);
	}

	public long getVirtualNodeId(){
		return ((SetVirtualIdRequest) request).getVirtualNodeId();
	}
}
