package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class VirtualLinkMessageCommand extends Command {
	private ChannelBuffer buffer;

	public VirtualLinkMessageCommand(byte commandType, byte requestID, byte rssi, byte lqi, byte len, long dest,
									 long source, ChannelBuffer payload) {
		super(commandType, requestID, payload);
		this.buffer = ChannelBuffers.buffer(19);
		this.buffer.writeByte(rssi);
		this.buffer.writeByte(lqi);
		this.buffer.writeByte(len);
		this.buffer.writeLong(dest);
		this.buffer.writeLong(source);
	}

	public byte getRssi() {
		return this.buffer.getByte(0);
	}

	public byte getLqi() {
		return this.buffer.getByte(1);
	}

	public byte getLen() {
		return this.buffer.getByte(2);
	}

	public long getDest() {
		return this.buffer.getLong(3);
	}

	public long getSource() {
		return this.buffer.getLong(11);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}

}
