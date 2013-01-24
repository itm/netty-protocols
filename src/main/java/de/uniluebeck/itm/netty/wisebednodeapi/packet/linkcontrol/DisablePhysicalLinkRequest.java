package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 10:24
 * To change this template use File | Settings | File Templates.
 */
public class DisablePhysicalLinkRequest extends Request {

	private final long nodeB;

	public DisablePhysicalLinkRequest(final byte requestID, final byte[] payload, final long nodeB) {
		super(CommandType.LinkControl.DISABLE_PHYSICAL_LINK, requestID, payload);
		this.nodeB = nodeB;
	}

	public long getNodeB() {
		return this.nodeB;
	}
}
