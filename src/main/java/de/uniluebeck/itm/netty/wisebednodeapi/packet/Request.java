package de.uniluebeck.itm.netty.wisebednodeapi.packet;

/**
 * Created by IntelliJ IDEA. User: bimschas Date: 08.06.11 Time: 16:05 TODO change
 */
public abstract class Request {

	private final byte commandType;

	private final byte requestID;

	private final byte[] payload;

	public Request(final byte commandType, final byte requestID, final byte[] payload) {
		this.commandType = commandType;
		this.requestID = requestID;
		this.payload = payload;
	}

	public byte getRequestID() {
		return requestID;
	}

	public byte getCommandType() {
		return this.commandType;
	}

	public byte[] getPayload() {
		return payload;
	}
}
