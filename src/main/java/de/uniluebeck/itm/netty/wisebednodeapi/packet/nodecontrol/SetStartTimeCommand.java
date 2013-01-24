package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:22
 * To change this template use File | Settings | File Templates.
 */
public class SetStartTimeCommand extends Command {

	private ChannelBuffer buffer;

	public SetStartTimeCommand(byte requestID, ChannelBuffer payload, short time) {
		super(CommandType.NodeControl.SET_START_TIME, requestID, payload);
		this.buffer = ChannelBuffers.buffer(2);
		this.buffer.writeShort(time);
	}

	public short getTime() {
		return this.buffer.getShort(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
