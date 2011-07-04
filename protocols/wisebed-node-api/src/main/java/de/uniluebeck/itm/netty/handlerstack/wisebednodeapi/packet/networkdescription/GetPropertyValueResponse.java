package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
public class GetPropertyValueResponse extends Response{
	public GetPropertyValueResponse(final GetPropertyValueRequest request) {
		super(request);
	}

	public byte getProperty(){
		return ((GetPropertyValueRequest) request).getProperty();
	}
}
