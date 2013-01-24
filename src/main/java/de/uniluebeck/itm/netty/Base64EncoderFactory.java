package de.uniluebeck.itm.netty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.util.PropertiesHelper;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.base64.Base64Encoder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Base64EncoderFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		Boolean breakLines = PropertiesHelper.getBooleanFromProperties(properties, "breakLines");

		Base64Encoder encoder;
		if (breakLines != null) {
			encoder = new Base64Encoder(breakLines);
		} else {
			encoder = new Base64Encoder();
		}

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> configurationOptions = HashMultimap.create();
		configurationOptions.put("breakLines", "(boolean)");
		return configurationOptions;
	}

	@Override
	public String getDescription() {
		return "Encodes a ChannelBuffer into a Base64-encoded ChannelBuffer. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/base64/Base64Encoder.html.";
	}

	@Override
	public String getName() {
		return "base64-encoder";
	}
}
