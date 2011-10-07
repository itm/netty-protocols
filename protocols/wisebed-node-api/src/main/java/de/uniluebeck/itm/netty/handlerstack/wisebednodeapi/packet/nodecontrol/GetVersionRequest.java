package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class GetVersionRequest extends Request{
	public GetVersionRequest(final byte requestID, final byte[] payload) {
		super(CommandType.NodeControl.GET_VERSION, requestID, payload);
	}
}
