package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
public class VirtualLinkDataCommand extends VirtualLinkMessageCommand {

	public VirtualLinkDataCommand(byte requestID, byte rssi, byte lqi, byte len, long dest, long source,
								  ChannelBuffer payload) {
		super(CommandType.Interaction.VIRTUAL_LINK_DATA, requestID, rssi, lqi, len, dest, source, payload);
	}
}
