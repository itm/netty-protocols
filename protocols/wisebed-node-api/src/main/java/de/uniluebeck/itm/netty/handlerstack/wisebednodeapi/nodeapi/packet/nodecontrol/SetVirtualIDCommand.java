package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.Command;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:28
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIdCommand extends Command {
	private ChannelBuffer buffer;
	public SetVirtualIdCommand(byte requestID, ChannelBuffer payload, long virtualNodeId) {
		super(CommandType.NodeControl.SET_VIRTUAL_ID, requestID, payload);
		this.buffer = ChannelBuffers.buffer(8);
		this.buffer.writeLong(virtualNodeId);
	}

	public long getVirtualNodeId(){
		return this.buffer.getLong(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
