package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class FlashProgramDataCommand extends FlashProgramMessageCommand {
	public FlashProgramDataCommand(byte requestID, byte len, ChannelBuffer payload) {
		super(CommandType.Interaction.FLASH_PROGRAM_DATA, requestID, len, payload);
	}
}
