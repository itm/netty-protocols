package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class BinaryDataRequest extends Request {
	private final byte len;

	public BinaryDataRequest(final byte requestID, final byte len, final byte[] payload) {
		super(CommandType.Interaction.BINARY_DATA, requestID, payload);
		this.len = len;
	}

	public byte getLen() {
		return this.len;
	}
}
