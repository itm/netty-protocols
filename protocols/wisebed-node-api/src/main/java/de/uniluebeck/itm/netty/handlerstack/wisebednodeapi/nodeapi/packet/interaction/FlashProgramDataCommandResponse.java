package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class FlashProgramDataCommandResponse extends CommandResponse {
	public FlashProgramDataCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.FLASH_PROGRAM_DATA, requestID, result, payload);
	}
}
