package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 11:56
 * To change this template use File | Settings | File Templates.
 */
public class FlashProgramDataRequest extends Request {

	private final byte len;

	public FlashProgramDataRequest(final byte requestID, final byte len, final byte[] payload) {
		super(CommandType.Interaction.FLASH_PROGRAM_DATA, requestID, payload);
		this.len = len;
	}

	public byte getLen() {
		return len;
	}
}
