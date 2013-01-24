package de.uniluebeck.itm.netty.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class GetPropertyValueCommand extends Command {

	private ChannelBuffer buffer;

	public GetPropertyValueCommand(byte requestID, ChannelBuffer payload, byte property) {
		super(CommandType.NetworkDescription.GET_PROPERTY_VALUE, requestID, payload);
		this.buffer = ChannelBuffers.buffer(1);
		this.buffer.writeByte(property);
	}

	public byte getProperty() {
		return this.buffer.getByte(0);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return ChannelBuffers.wrappedBuffer(super.buffer, this.buffer);
	}
}
