package de.uniluebeck.itm.netty.handlerstack.contiki;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ContikiFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {
		return newArrayList();
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(Multimap<String, String> properties) throws Exception {
		return create(null, properties);
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
