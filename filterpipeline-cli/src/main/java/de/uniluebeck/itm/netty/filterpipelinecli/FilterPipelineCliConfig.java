package de.uniluebeck.itm.netty.filterpipelinecli;

import de.uniluebeck.itm.wsn.drivers.factories.DeviceType;

import java.io.File;

public class FilterPipelineCliConfig {

	private final File filterPipelineConfigurationFile;
	
	private final String port;

	private final DeviceType deviceType;

	public FilterPipelineCliConfig(final String port, final File filterPipelineConfigurationFile, final DeviceType deviceType) {
		this.port = port;
		this.filterPipelineConfigurationFile = filterPipelineConfigurationFile;
		this.deviceType = deviceType;
	}

	public String getPort() {
		return port;
	}

	public File getFilterPipelineConfigurationFile() {
		return filterPipelineConfigurationFile;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}
}
