package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */
public class AreNodesAliveCommand extends Command {

	public AreNodesAliveCommand(byte requestID, ChannelBuffer payload) {
		super(CommandType.NodeControl.ARE_NODES_ALIVE, requestID, payload);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return super.buffer;
	}
}
