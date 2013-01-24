package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class GetVersionCommandResponse extends CommandResponse {

	public GetVersionCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NodeControl.GET_VERSION, requestID, result, payload);
	}
}
