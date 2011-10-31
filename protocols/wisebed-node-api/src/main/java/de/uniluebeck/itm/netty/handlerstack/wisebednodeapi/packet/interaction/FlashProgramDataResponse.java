package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class FlashProgramDataResponse extends Response {
	public FlashProgramDataResponse(final FlashProgramDataRequest request) {
		super(request);
	}

	public byte getLen(){
		return ((FlashProgramDataRequest) request).getLen();
	}
}
