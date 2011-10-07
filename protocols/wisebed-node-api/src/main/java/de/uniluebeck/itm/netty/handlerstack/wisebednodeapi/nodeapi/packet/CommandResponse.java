package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public abstract class CommandResponse {
	protected ChannelBuffer buffer;

	public CommandResponse(byte commandType, byte requestID, byte result, ChannelBuffer payload) {
		buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(commandType);
		buffer.writeByte(requestID);
		buffer.writeByte(result);
		buffer.writeBytes(payload);
	}

	public ChannelBuffer getBuffer() {
		return this.buffer;
	}

	public byte getCommandType(){
		return buffer.getByte(0);
	}

	public byte getRequestID() {
		return buffer.getByte(1);
	}

	public byte getResult(){
		return buffer.getByte(2);
	}

	public ChannelBuffer getPayload(){
		return buffer.slice(3, buffer.readableBytes() - 3);
	}
}
