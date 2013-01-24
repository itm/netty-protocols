package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputBinaryCommand extends BinaryMessageCommand {
	public NodeOutputBinaryCommand(byte requestID, byte len, ChannelBuffer payload) {
		super(CommandType.Interaction.NODE_OUTPUT_BINARY, requestID, len, payload);
	}
}
