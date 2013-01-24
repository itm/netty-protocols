package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class EnablePhysicalLinkCommand extends Command {

	private ChannelBuffer buffer;

	public EnablePhysicalLinkCommand(byte requestID, ChannelBuffer payload, long nodeB) {
		super(CommandType.LinkControl.ENABLE_PHYSICAL_LINK, requestID, payload);
		buffer = ChannelBuffers.buffer(8);
		buffer.writeLong(nodeB);
	}

	public long getNodeB() {
		return buffer.getLong(0);
	}

	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

}
