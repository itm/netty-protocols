package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class SetStartTimeCommandResponse extends CommandResponse {

	public SetStartTimeCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.SET_START_TIME, requestID, result, payload);
	}
}
