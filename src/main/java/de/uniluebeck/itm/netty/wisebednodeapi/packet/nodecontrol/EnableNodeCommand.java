package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;


import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class EnableNodeCommand extends Command {

	public EnableNodeCommand(byte requestID, ChannelBuffer payload) {
		super(CommandType.NodeControl.ENABLE_NODE, requestID, payload);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return super.buffer;
	}
}
