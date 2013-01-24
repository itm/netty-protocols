package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIDRequest extends Request {

	private final long virtualNodeId;

	public SetVirtualIDRequest(final byte requestID, final byte[] payload, final long virtualNodeId) {
		super(CommandType.NodeControl.SET_VIRTUAL_ID, requestID, payload);
		this.virtualNodeId = virtualNodeId;
	}

	public long getVirtualNodeId() {
		return virtualNodeId;
	}
}
