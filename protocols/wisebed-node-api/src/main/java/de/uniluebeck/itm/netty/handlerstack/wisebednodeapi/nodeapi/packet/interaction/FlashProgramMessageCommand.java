package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.Command;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class FlashProgramMessageCommand extends Command {
	private ChannelBuffer buffer;

	public FlashProgramMessageCommand(byte commandType, byte requestID, byte len, ChannelBuffer payload) {
		super(commandType, requestID, payload);
		this.buffer = ChannelBuffers.buffer(1);
		this.buffer.writeByte(len);
	}

	public byte getLen() {
		return this.buffer.getByte(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

}
