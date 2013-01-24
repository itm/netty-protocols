package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class TextMessageCommand extends Command {

	private ChannelBuffer buffer;

	public TextMessageCommand(byte commandType, byte requestID, byte messageLevel, ChannelBuffer text) {
		super(commandType, requestID, text);
		this.buffer = ChannelBuffers.buffer(1);
		this.buffer.writeByte(messageLevel);
	}

	public byte getMessageLevel() {
		return this.buffer.getByte(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

}
