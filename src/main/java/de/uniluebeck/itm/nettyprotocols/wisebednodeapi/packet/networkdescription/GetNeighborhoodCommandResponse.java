package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class GetNeighborhoodCommandResponse extends CommandResponse {

	public GetNeighborhoodCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NetworkDescription.GET_NEIGHBORHOOD, requestID, result, payload);
	}
}
