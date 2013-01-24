package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class TextDataCommand extends TextMessageCommand {

	public TextDataCommand(byte requestID, byte messageLevel, ChannelBuffer text) {
		super(CommandType.Interaction.TEXT_DATA, requestID, messageLevel, text);
	}
}
