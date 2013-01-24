package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class DisableNodeRequest extends Request {

	public DisableNodeRequest(final byte requestID, final byte[] payload) {
		super(CommandType.NodeControl.DISABLE_NODE, requestID, payload);
	}
}
