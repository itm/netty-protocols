package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.Multimap;

import java.io.Serializable;

public class ChannelHandlerConfig implements Serializable {

	private String handlerName;

	private String instanceName;

	private Multimap<String, String> properties;

	public ChannelHandlerConfig(final String handlerName) {
		this.handlerName = handlerName;
	}

	public ChannelHandlerConfig(final String handlerName, final String instanceName) {
		this.handlerName = handlerName;
		this.instanceName = instanceName;
	}

	public ChannelHandlerConfig(final String handlerName, final String instanceName,
								final Multimap<String, String> properties) {
		this.handlerName = handlerName;
		this.instanceName = instanceName;
		this.properties = properties;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public void setHandlerName(final String handlerName) {
		this.handlerName = handlerName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(final String instanceName) {
		this.instanceName = instanceName;
	}

	public Multimap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Multimap<String, String> properties) {
		this.properties = properties;
	}
}
