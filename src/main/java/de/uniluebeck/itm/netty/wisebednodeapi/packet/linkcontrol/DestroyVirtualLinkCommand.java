package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class DestroyVirtualLinkCommand extends Command {
	private ChannelBuffer buffer;

	public DestroyVirtualLinkCommand(byte requestID, ChannelBuffer payload, long destinationNode) {
		super(CommandType.LinkControl.DESTROY_VIRTUAL_LINK, requestID, payload);
		this.buffer = ChannelBuffers.buffer(8);
		this.buffer.writeLong(destinationNode);
	}

	public long getDestinationNode() {
		return this.buffer.getLong(0);
	}

	public ChannelBuffer getBuffer(){
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

}
