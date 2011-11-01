package de.uniluebeck.itm.netty.handlerstack.wisebednodeapi.nodeapi.packet;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.06.11
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class CommandType {
	public class Interaction {
		public final static byte TEXT_DATA = 10;
		public final static byte VIRTUAL_LINK_DATA = 11;
		public final static byte BINARY_DATA = 12;
		public final static byte FLASH_PROGRAM_DATA = 13;

		public final static byte NODE_OUTPUT_TEXT = 50;
		public final static byte NODE_OUTPUT_BINARY = 51;
		public final static byte NODE_OUTPUT_VIRTUAL_LINK = 52;
	}

	public class NodeControl {
		public final static byte ENABLE_NODE = 20;
		public final static byte DISABLE_NODE = 21;
		public final static byte RESET_NODE = 22;
		public final static byte SET_START_TIME = 23;
		public final static byte SET_VIRTUAL_ID = 24;
		public final static byte ARE_NODES_ALIVE = 25;
		public final static byte GET_VERSION = 26;
	}

	public class LinkControl {
		public final static byte SET_VIRTUAL_LINK = 30;
		public final static byte DESTROY_VIRTUAL_LINK = 31;
		public final static byte ENABLE_PHYSICAL_LINK = 32;
		public final static byte DISABLE_PHYSICAL_LINK = 33;
	}

	public class NetworkDescription {
		public final static byte GET_PROPERTY_VALUE = 40;
		public final static byte GET_NEIGHBORHOOD = 41;
	}
}
