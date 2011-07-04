package de.uniluebeck.itm.netty.handlerstack.wisebed;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WisebedMulticastAddress extends SocketAddress {

	private final Set<String> nodeUrns;

	private final Map<String, Object> userContext;

	public WisebedMulticastAddress(final Set<String> nodeUrns, final Map<String, Object> userContext) {
		this.nodeUrns = nodeUrns;
		this.userContext = userContext;
	}

	public WisebedMulticastAddress(final Set<String> nodeUrns) {
		this.nodeUrns = nodeUrns;
		this.userContext = new HashMap<String, Object>();
	}

	public Set<String> getNodeUrns() {
		return nodeUrns;
	}

	public Map<String, Object> getUserContext() {
		return userContext;
	}
}