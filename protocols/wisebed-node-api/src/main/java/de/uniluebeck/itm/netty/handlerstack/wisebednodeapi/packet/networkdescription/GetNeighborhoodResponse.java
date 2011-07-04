package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 11:02
 * To change this template use File | Settings | File Templates.
 */
public class GetNeighborhoodResponse extends Response{
	public GetNeighborhoodResponse(final GetNeighborhoodRequest request) {
		super(request);
	}
}
