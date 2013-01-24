package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public class EnablePhysicalLinkRequest extends Request {

	private final long nodeB;

	public EnablePhysicalLinkRequest(final byte requestID, final byte[] payload, final long nodeB) {
		super(CommandType.LinkControl.ENABLE_PHYSICAL_LINK, requestID, payload);
		this.nodeB = nodeB;
	}

	public long getNodeB() {
		return nodeB;
	}

}
