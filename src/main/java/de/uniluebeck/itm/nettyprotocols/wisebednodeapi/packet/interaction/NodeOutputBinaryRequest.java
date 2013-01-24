package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputBinaryRequest extends Request {

	private final byte len;

	public NodeOutputBinaryRequest(final byte requestID, final byte len, final byte[] payload) {
		super(CommandType.Interaction.NODE_OUTPUT_BINARY, requestID, payload);
		this.len = len;
	}

	public byte getLen() {
		return len;
	}
}
