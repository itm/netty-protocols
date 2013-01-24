package de.uniluebeck.itm.netty.tinyos;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.HandlerFactory;
import de.uniluebeck.itm.netty.tinyos.HdlcTranslateDecoderFactory;
import de.uniluebeck.itm.netty.tinyos.HdlcTranslateEncoderFactory;
import de.uniluebeck.itm.netty.tinyos.TinyOsSerialDecoderFactory;
import de.uniluebeck.itm.netty.tinyos.TinyOsSerialEncoderFactory;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class TinyOsFactory implements HandlerFactory {

	@Override
	public List<Tuple<String, ChannelHandler>> create(@Nullable final String instanceName,
													  final Multimap<String, String> properties) throws Exception {

		List<Tuple<String, ChannelHandler>> handlers = new LinkedList<Tuple<String, ChannelHandler>>();
		handlers.addAll(new HdlcTranslateDecoderFactory().create(instanceName + "-tinyos-hdlctranslate-decoder", properties));
		handlers.addAll(new HdlcTranslateEncoderFactory().create(instanceName + "-tinyos-hdlctranslate-encoder", properties));
		handlers.addAll(new TinyOsSerialDecoderFactory().create(instanceName + "-tinyos-serial-decoder", properties));
		handlers.addAll(new TinyOsSerialEncoderFactory().create(instanceName + "-tinyos-serial-encoder", properties));
		return handlers;
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
		return "Protocol stack consisting of tinyos-hdlctranslate and tinyos-serial protocols. For more details see http://www.tinyos.net/tinyos-2.x/doc/html/tep113.html";
	}

	@Override
	public String getName() {
		return "tinyos";
	}
}
