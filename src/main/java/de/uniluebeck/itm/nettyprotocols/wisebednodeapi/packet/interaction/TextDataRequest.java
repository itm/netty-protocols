package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class TextDataRequest extends Request {

	private final byte messageLevel;

	public TextDataRequest(final byte requestID, final byte messageLevel, final byte[] payload) {
		super(CommandType.Interaction.TEXT_DATA, requestID, payload);
		this.messageLevel = messageLevel;
	}

	public byte getMessageLevel() {
		return messageLevel;
	}
}
