package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandResponse;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class TextDataCommandResponse extends CommandResponse {
	public TextDataCommandResponse(byte requestID, byte result, ChannelBuffer payload) {
		super(CommandType.Interaction.TEXT_DATA, requestID, result, payload);
	}
}
