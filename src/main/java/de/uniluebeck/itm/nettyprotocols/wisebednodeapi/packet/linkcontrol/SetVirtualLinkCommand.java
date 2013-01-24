package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualLinkCommand extends Command {

	private ChannelBuffer buffer;

	public SetVirtualLinkCommand(byte requestID, ChannelBuffer payload, long destinationNode) {
		super(CommandType.LinkControl.SET_VIRTUAL_LINK, requestID, payload);
		buffer = ChannelBuffers.buffer(8);
		buffer.writeLong(destinationNode);
	}

	public long getDestinationNode() {
		return this.buffer.getLong(0);
	}

	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
