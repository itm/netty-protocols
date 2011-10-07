package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class BinaryDataCommand extends BinaryMessageCommand {
	public BinaryDataCommand(byte requestID, byte len, ChannelBuffer payload) {
		super(CommandType.Interaction.BINARY_DATA, requestID, len, payload);
	}
}
