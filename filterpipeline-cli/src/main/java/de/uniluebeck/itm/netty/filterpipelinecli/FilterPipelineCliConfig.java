package de.uniluebeck.itm.netty.filterpipelinecli;

import java.io.File;

public class FilterPipelineCliConfig {

	private final File filterPipelineConfigurationFile;
	
	private final String port;

	public FilterPipelineCliConfig(final String port, final File filterPipelineConfigurationFile) {
		this.port = port;
		this.filterPipelineConfigurationFile = filterPipelineConfigurationFile;
	}

	public String getPort() {
		return port;
	}

	public File getFilterPipelineConfigurationFile() {
		return filterPipelineConfigurationFile;
	}
}
