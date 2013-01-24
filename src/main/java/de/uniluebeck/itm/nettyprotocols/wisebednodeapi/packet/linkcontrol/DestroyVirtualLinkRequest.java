package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public class DestroyVirtualLinkRequest extends Request {

	private final long destinationNode;

	public DestroyVirtualLinkRequest(final byte requestID, final byte payload[], final long destinationNode) {
		super(CommandType.LinkControl.DESTROY_VIRTUAL_LINK, requestID, payload);
		this.destinationNode = destinationNode;
	}

	public long getDestinationNode() {
		return destinationNode;
	}
}
