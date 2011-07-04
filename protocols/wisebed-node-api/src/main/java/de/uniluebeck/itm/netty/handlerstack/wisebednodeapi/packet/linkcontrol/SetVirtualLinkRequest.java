package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 08.06.11 Time: 15:19 TODO change
 */
public class SetVirtualLinkRequest extends Request {

	private final long destinationNode;

	public SetVirtualLinkRequest(final byte requestID, final byte payload[], final long destinationNode) {
		super(CommandType.LinkControl.SET_VIRTUAL_LINK, requestID, payload);
		this.destinationNode = destinationNode;
	}

	public long getDestinationNode() {
		return destinationNode;
	}
}
