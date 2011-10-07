package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIdRequest extends Request {
	private final long virtualNodeId;

	public SetVirtualIdRequest(final byte requestID, final byte[] payload, final long virtualNodeId) {
		super(CommandType.NodeControl.SET_VIRTUAL_ID, requestID, payload);
		this.virtualNodeId = virtualNodeId;
	}

	public long getVirtualNodeId() {
		return virtualNodeId;
	}
}
