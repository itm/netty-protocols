package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public class SetVirtualIDCommandResponse extends CommandResponse {
	public SetVirtualIDCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.SET_VIRTUAL_ID, requestID, result, payload);
	}
}
