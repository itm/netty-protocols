package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:28
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIDCommand extends Command {

	private ChannelBuffer buffer;

	public SetVirtualIDCommand(byte requestID, ChannelBuffer payload, long virtualNodeId) {
		super(CommandType.NodeControl.SET_VIRTUAL_ID, requestID, payload);
		this.buffer = ChannelBuffers.buffer(8);
		this.buffer.writeLong(virtualNodeId);
	}

	public long getVirtualNodeId() {
		return this.buffer.getLong(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
