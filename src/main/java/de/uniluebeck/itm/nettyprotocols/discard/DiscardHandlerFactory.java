package de.uniluebeck.itm.nettyprotocols.discard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.ChannelHandlerConfig;
import de.uniluebeck.itm.nettyprotocols.HandlerFactory;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandler;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandlerList;

import static de.uniluebeck.itm.nettyprotocols.util.HandlerFactoryPropertiesHelper.getFirstValueOf;

public class DiscardHandlerFactory implements HandlerFactory {

	private static final String KEY_DISCARD_UPSTREAM = "discardUpstream";

	private static final String KEY_DISCARD_DOWNSTREAM = "discardDownstream";

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {

		final boolean discardUpstream = getFirstValueOf(config.getProperties(), KEY_DISCARD_UPSTREAM, true);
		final boolean discardDownstream = getFirstValueOf(config.getProperties(), KEY_DISCARD_DOWNSTREAM, true);

		return new NamedChannelHandlerList(new NamedChannelHandler(
				config.getInstanceName(),
				new DiscardHandler(discardUpstream, discardDownstream)
		));
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(KEY_DISCARD_UPSTREAM, "(optional, boolean, default=true) if true all upstream messages are discarded");
		map.put(KEY_DISCARD_DOWNSTREAM,
				"(optional, boolean, default=true) if true all downstream messages are discarded"
		);
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
