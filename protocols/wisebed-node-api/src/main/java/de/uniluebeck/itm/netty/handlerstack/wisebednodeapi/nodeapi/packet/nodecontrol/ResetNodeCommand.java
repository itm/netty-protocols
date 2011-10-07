package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.Command;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class ResetNodeCommand extends Command {
	private ChannelBuffer buffer;

	public ResetNodeCommand(byte requestID, ChannelBuffer payload, short time) {
		super(CommandType.NodeControl.RESET_NODE, requestID, payload);
		this.buffer = ChannelBuffers.buffer(2);
		this.buffer.writeShort(time);
	}

	public short getTime(){
		return this.buffer.getShort(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
