package de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.interaction;

import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.CommandType;
import de.uniluebeck.itm.nettyprotocols.wisebednodeapi.packet.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 01.07.11
 * Time: 12:29
 * To change this template use File | Settings | File Templates.
 */
public class VirtualLinkDataRequest extends Request {

	private final byte rssi;

	private final byte lqi;

	private final byte len;

	private final long dest;

	private final long source;

	public VirtualLinkDataRequest(final byte requestID, final byte rssi, final byte lqi, final byte len,
								  final long dest, final long source, final byte[] payload) {
		super(CommandType.Interaction.VIRTUAL_LINK_DATA, requestID, payload);
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
