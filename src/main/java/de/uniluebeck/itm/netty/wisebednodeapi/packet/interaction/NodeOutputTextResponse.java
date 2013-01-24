package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputTextResponse extends Response {

	public NodeOutputTextResponse(final NodeOutputTextRequest request) {
		super(request);
	}

	public byte getMessageLevel() {
		return ((NodeOutputTextRequest) request).getMessageLevel();
	}
}
