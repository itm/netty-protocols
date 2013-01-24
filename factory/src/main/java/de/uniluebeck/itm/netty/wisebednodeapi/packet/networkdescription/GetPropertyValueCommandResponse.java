package de.uniluebeck.itm.netty.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class GetPropertyValueCommandResponse extends CommandResponse {
	public GetPropertyValueCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.NetworkDescription.GET_PROPERTY_VALUE, requestID, result, payload);
	}
}
