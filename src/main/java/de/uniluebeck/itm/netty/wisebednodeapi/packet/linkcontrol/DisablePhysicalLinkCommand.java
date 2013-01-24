package de.uniluebeck.itm.netty.wisebednodeapi.packet.linkcontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */
public class DisablePhysicalLinkCommand extends Command {
	private ChannelBuffer buffer;

	public DisablePhysicalLinkCommand(byte requestID, ChannelBuffer payload, long nodeB) {
		super(CommandType.LinkControl.DISABLE_PHYSICAL_LINK, requestID, payload);
		this.buffer = ChannelBuffers.buffer(8);
		this.buffer.writeLong(nodeB);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

	public long getNodeB() {
		return this.buffer.getLong(0);
	}
}
