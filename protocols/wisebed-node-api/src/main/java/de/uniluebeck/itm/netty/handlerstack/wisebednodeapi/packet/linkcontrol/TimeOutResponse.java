package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 10.06.11
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class TimeOutResponse extends Response {

	public TimeOutResponse(final Request request) {
		super(request);
	}
}
