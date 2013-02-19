package de.uniluebeck.itm.nettyprotocols;

import org.jboss.netty.channel.ChannelHandler;

import static com.google.common.base.Preconditions.checkNotNull;

public class NamedChannelHandler {

	private final String instanceName;

	private final ChannelHandler channelHandler;

	public NamedChannelHandler(final String instanceName, final ChannelHandler channelHandler) {
		this.instanceName = checkNotNull(instanceName);
		this.channelHandler = checkNotNull(channelHandler);
	}

	public String getInstanceName() {
		return instanceName;
	}

	public ChannelHandler getChannelHandler() {
		return channelHandler;
	}
}
