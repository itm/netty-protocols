package de.uniluebeck.itm.nettyprotocols.contiki;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.ChannelHandlerConfig;
import de.uniluebeck.itm.nettyprotocols.HandlerFactory;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandlerList;

public class ContikiHandlerFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList();
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "An empty protocol stack suitable for serial communication with the Contiki operating system.";
	}

	@Override
	public String getName() {
		return "contiki";
	}
}
