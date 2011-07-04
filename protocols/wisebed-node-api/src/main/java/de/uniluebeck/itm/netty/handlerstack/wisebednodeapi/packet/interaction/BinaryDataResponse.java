package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class BinaryDataResponse extends Response {
	public BinaryDataResponse(final BinaryDataRequest request) {
		super(request);
	}

	public byte getLen() {
		return ((BinaryDataRequest) request).getLen();
	}
}
