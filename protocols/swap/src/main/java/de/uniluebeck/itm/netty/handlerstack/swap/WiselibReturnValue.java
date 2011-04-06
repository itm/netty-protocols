package de.uniluebeck.itm.netty.handlerstack.swap;

/**
 * Default values returned by the Wiselib, e.g. in a response message to a request message.
 */
public enum WiselibReturnValue {

	/**
	 * Default return value of success
	 */
	SUCCESS((byte) (0xFF & 0)),

	/**
	 * Unspecified error value - if no other fits
	 */
	ERR_UNSPEC((byte) (0xFF & -1)),

	/**
	 * Out of memory
	 */
	ERR_NOMEM((byte) (0xFF & 12)),

	/**
	 * Device or resource busy - try again later
	 */
	ERR_BUSY((byte) (0xFF & 16)),

	/**
	 * Function not implemented
	 */
	ERR_NOTIMPL((byte) (0xFF & 38)),

	/**
	 * Network is down
	 */
	ERR_NETDOWN((byte) (0xFF & 100)),

	/**
	 * No route to host
	 */
	ERR_HOSTUNREACH((byte) (0xFF & 113));

	private byte value;

	private WiselibReturnValue(byte value) {
		this.value = value;
	}

	public static WiselibReturnValue fromValue(byte byteValue) {
		for (WiselibReturnValue wiselibReturnValue : WiselibReturnValue.values()) {
			if (wiselibReturnValue.value == byteValue) {
				return wiselibReturnValue;
			}
		}
		throw new IllegalArgumentException("Unknown byte value: " + byteValue);
	}

	public byte getValue() {
		return value;
	}
}
