package de.uniluebeck.itm.netty.wisebednodeapi.packet.networkdescription;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.Command;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class GetNeighborhoodCommand extends Command {

	public GetNeighborhoodCommand(byte requestID, ChannelBuffer payload) {
		super(CommandType.NetworkDescription.GET_NEIGHBORHOOD, requestID, payload);
	}

	@Override
	public ChannelBuffer getBuffer() {
		return super.buffer;
	}
}
