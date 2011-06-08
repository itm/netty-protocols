package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 08.06.11 Time: 15:19 TODO change
 */
public class SetVirtualLinkRequest extends Request {

	private final long destinationNode;

	public SetVirtualLinkRequest(final long destinationNode) {
		this.destinationNode = destinationNode;
	}

	public long getDestinationNode() {
		return destinationNode;
	}
}
