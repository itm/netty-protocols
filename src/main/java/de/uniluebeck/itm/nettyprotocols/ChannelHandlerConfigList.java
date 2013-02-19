package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class ChannelHandlerConfigList extends LinkedList<ChannelHandlerConfig> implements Serializable {

	public ChannelHandlerConfigList() {
	}

	public ChannelHandlerConfigList(final ChannelHandlerConfigList channelHandlerConfigs) {
		addAll(channelHandlerConfigs);
	}

	public ChannelHandlerConfigList(final Collection<? extends ChannelHandlerConfig> c) {
		super(c);
	}

	public ChannelHandlerConfigList(final ChannelHandlerConfig config, final ChannelHandlerConfig... moreConfigs) {
		add(config);
		for (ChannelHandlerConfig anotherConfig : moreConfigs) {
			add(anotherConfig);
		}
	}

	public boolean add(final String handlerName, final String instanceName, final Multimap<String, String> properties) {
		return add(new ChannelHandlerConfig(handlerName, instanceName, properties));
	}
}
