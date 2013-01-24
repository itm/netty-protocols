package de.uniluebeck.itm.netty.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputVirtualLinkCommand extends VirtualLinkMessageCommand {
	public NodeOutputVirtualLinkCommand(byte requestID, byte rssi, byte lqi, byte len, long dest, long source,
										ChannelBuffer payload) {
		super(CommandType.Interaction.NODE_OUTPUT_VIRTUAL_LINK, requestID, rssi, lqi, len, dest, source, payload);
	}
}
