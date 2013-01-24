package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputTextCommand extends TextMessageCommand {

	public NodeOutputTextCommand(byte requestID, byte messageLevel, ChannelBuffer payload) {
		super(CommandType.Interaction.NODE_OUTPUT_TEXT, requestID, messageLevel, payload);
	}
}
