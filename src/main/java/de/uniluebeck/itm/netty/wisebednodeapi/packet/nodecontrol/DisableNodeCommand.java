package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
public class DisableNodeCommand extends Command {
	public DisableNodeCommand(byte requestID, ChannelBuffer payload) {
		super(CommandType.NodeControl.DISABLE_NODE, requestID, payload);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return super.buffer;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
