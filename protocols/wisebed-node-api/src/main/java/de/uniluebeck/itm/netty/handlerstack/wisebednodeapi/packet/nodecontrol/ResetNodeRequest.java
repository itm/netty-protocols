package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class ResetNodeRequest extends Request {
	private final short time;

	public ResetNodeRequest(final byte requestID, final byte[] payload, final short time) {
		super(CommandType.NodeControl.RESET_NODE, requestID, payload);
		this.time = time;
	}

	public short getTime() {
		return time;
	}
}
