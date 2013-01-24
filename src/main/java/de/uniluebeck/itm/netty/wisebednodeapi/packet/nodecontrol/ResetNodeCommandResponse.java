package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
public class ResetNodeCommandResponse extends CommandResponse {

	public ResetNodeCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.RESET_NODE, requestID, result, payload);
	}
}
