package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 08.06.11 Time: 15:37 TODO change
 */
public class SetVirtualLinkCommand {

	private ChannelBuffer buffer;

	public SetVirtualLinkCommand(long destinationNode, long requestId) {
		buffer = ChannelBuffers.buffer(16);
		buffer.writeLong(destinationNode);
		buffer.writeLong(requestId);
	}

	public long getDestinationNode() {
		return buffer.getLong(0);
	}

	public long getRequestId() {
		return buffer.getLong(8);
	}

	public ChannelBuffer getBuffer() {
		return buffer;
	}
}
