package de.uniluebeck.itm.netty.wisebednodeapi.packet.nodecontrol;

import de.uniluebeck.itm.netty.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.06.11
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class SetStartTimeRequest extends Request {

	private final short time;

	public SetStartTimeRequest(final byte requestID, final byte[] payload, final short time) {
		super(CommandType.NodeControl.SET_START_TIME, requestID, payload);
		this.time = time;

	}

	public short getTime() {
		return time;
	}
}
