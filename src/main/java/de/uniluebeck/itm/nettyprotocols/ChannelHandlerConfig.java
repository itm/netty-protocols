package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.Multimap;

import java.io.Serializable;

import static com.google.common.collect.Sets.newHashSet;

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

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final ChannelHandlerConfig that = (ChannelHandlerConfig) o;

		if (!handlerName.equals(that.handlerName) || !instanceName.equals(that.instanceName)) {
			return false;
		}

		if (!properties.keySet().equals(that.properties.keySet())) {
			return false;
		}

		for (String key : properties.keys()) {
			if (!newHashSet(properties.get(key)).equals(newHashSet(that.properties.get(key)))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = handlerName.hashCode();
		result = 31 * result + instanceName.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ChannelHandlerConfig{" +
				"handlerName='" + handlerName + '\'' +
				", instanceName='" + instanceName + '\'' +
				", properties=" + properties +
				"} " + super.toString();
	}
}
