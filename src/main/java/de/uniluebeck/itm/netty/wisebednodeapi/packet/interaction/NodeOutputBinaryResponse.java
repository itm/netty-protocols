package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputBinaryResponse extends Response {
	public NodeOutputBinaryResponse(final NodeOutputBinaryRequest request) {
		super(request);
	}

	public byte getLen(){
		return ((NodeOutputBinaryRequest) request).getLen();
	}
}
