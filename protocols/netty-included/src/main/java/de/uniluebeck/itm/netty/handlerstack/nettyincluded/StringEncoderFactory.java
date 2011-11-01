package de.uniluebeck.itm.netty.handlerstack.nettyincluded;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class StringEncoderFactory implements HandlerFactory {

	private static final String CHARSET_NAME = "charsetName";

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		final String charsetName = properties.get(CHARSET_NAME).iterator().next();

		final StringEncoder encoder = charsetName == null ?
				new StringEncoder(CharsetUtil.UTF_8) :
				new StringEncoder(Charset.forName(charsetName));

		return newArrayList(new Tuple<String, ChannelHandler>(instanceName, encoder));
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(final Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(CHARSET_NAME, "(int, optional, default=UTF-8) the character set to use (e.g. UTF-16, UTF-16BE, "
				+ "UTF-16LE, UTF-8, ISO-8859-1, US-ASCII)");
		return map;
	}

	@Override
	public String getDescription() {
		return "Encodes the requested String into a ChannelBuffer. See "
				+ "http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/codec/string/StringEncoder.html.";
	}

	@Override
	public String getName() {
		return "string-encoder";
	}
}
