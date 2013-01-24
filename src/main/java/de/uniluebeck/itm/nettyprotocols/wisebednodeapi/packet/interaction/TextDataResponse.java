package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:20
 * To change this template use File | Settings | File Templates.
 */
public class TextDataResponse extends Response {

	public TextDataResponse(final TextDataRequest request) {
		super(request);
	}

	public byte getMessageLevel() {
		return ((TextDataRequest) request).getMessageLevel();
	}
}
