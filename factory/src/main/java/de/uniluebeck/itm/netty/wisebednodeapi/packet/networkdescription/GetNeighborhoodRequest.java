package de.uniluebeck.itm.netty.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
public class GetNeighborhoodRequest extends Request {
	public GetNeighborhoodRequest(final byte requestID, final byte[] payload) {
		super(CommandType.NetworkDescription.GET_NEIGHBORHOOD, requestID, payload);
	}
}
