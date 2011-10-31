package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class DisableNodeCommandResponse extends CommandResponse {
	public DisableNodeCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.DISABLE_NODE, requestID, result, payload);
	}
}
