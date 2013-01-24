package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.06.11
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public abstract class Command {

	protected ChannelBuffer buffer;

	public Command(byte commandType, byte requestID, ChannelBuffer payload) {
		buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(commandType);
		buffer.writeByte(requestID);
		buffer.writeBytes(payload);
	}

	public abstract ChannelBuffer getBuffer();

	public int getCommandType() {
		return this.buffer.getByte(0);
	}

	public int getRequestID() {
		return this.buffer.getByte(1);
	}

	public ChannelBuffer getPayload() {
		return this.buffer.slice(2, this.buffer.readableBytes() - 2);
	}
}
