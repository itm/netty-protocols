package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
public class EnableNodeCommandResponse extends CommandResponse {
	public EnableNodeCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.ENABLE_NODE, requestID, result, payload);
	}
}
