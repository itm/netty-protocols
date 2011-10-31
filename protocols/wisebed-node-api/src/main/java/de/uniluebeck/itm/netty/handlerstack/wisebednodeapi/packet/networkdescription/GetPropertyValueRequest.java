package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class GetPropertyValueRequest extends Request {
	private final byte property;

	public GetPropertyValueRequest(final byte requestID, final byte[] payload, final byte property) {
		super(CommandType.NetworkDescription.GET_PROPERTY_VALUE, requestID, payload);
		this.property = property;
	}

	public byte getProperty() {
		return property;
	}
}
