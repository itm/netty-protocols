package de.uniluebeck.itm.netty.discard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.HandlerFactory;
import de.uniluebeck.itm.netty.util.HandlerFactoryPropertiesHelper;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DiscardHandlerFactory implements HandlerFactory {

	private static final String KEY_DISCARD_UPSTREAM = "discardUpstream";

	private static final String KEY_DISCARD_DOWNSTREAM = "discardDownstream";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties)
			throws Exception {

		final boolean discardUpstream = HandlerFactoryPropertiesHelper
				.getFirstValueOf(properties, KEY_DISCARD_UPSTREAM, true);
		final boolean discardDownstream = HandlerFactoryPropertiesHelper.getFirstValueOf(properties, KEY_DISCARD_DOWNSTREAM, true);;

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, new DiscardHandler(discardUpstream, discardDownstream)));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(KEY_DISCARD_UPSTREAM, "(optional, boolean, default=true) if true all upstream messages are discarded");
		map.put(KEY_DISCARD_DOWNSTREAM, "(optional, boolean, default=true) if true all downstream messages are discarded");
		return map;
	}

	@Override
	public String getDescription() {
		return "Discards upstream and downstream messages";
	}

	@Override
	public String getName() {
		return "discard";
	}
}
