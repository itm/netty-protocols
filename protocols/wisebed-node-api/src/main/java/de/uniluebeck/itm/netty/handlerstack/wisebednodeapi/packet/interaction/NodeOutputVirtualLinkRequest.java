package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet.CommandType;
import de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.07.11
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputVirtualLinkRequest extends Request {
	private final byte rssi;
	private final byte lqi;
	private final byte len;
	private final long dest;
	private final long source;

	public NodeOutputVirtualLinkRequest(final byte requestID, final byte rssi, final byte lqi, final byte len,
										final long dest, final long source, byte[] payload) {
		super(CommandType.Interaction.NODE_OUTPUT_VIRTUAL_LINK, requestID, payload);
		this.rssi = rssi;
		this.lqi = lqi;
		this.len = len;
		this.dest = dest;
		this.source = source;
	}

	public byte getRssi() {
		return rssi;
	}

	public byte getLqi() {
		return lqi;
	}

	public byte getLen() {
		return len;
	}

	public long getDest() {
		return dest;
	}

	public long getSource() {
		return source;
	}
}
