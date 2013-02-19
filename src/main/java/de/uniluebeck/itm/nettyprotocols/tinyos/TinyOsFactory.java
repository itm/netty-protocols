package de.uniluebeck.itm.nettyprotocols.tinyos;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.ChannelHandlerConfig;
import de.uniluebeck.itm.nettyprotocols.HandlerFactory;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandler;
import de.uniluebeck.itm.nettyprotocols.NamedChannelHandlerList;

public class TinyOsFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		return new NamedChannelHandlerList(
				new NamedChannelHandler(
						config.getInstanceName() + "-tinyos-hdlctranslate-decoder",
						new HdlcTranslateDecoder(config.getInstanceName() + "-tinyos-hdlctranslate-decoder")
				),
				new NamedChannelHandler(
						config.getInstanceName() + "-tinyos-hdlctranslate-encoder",
						new HdlcTranslateEncoder(config.getInstanceName() + "-tinyos-hdlctranslate-encoder")
				),
				new NamedChannelHandler(
						config.getInstanceName() + "-tinyos-serial-decoder",
						new TinyOsSerialDecoder(config.getInstanceName() + "-tinyos-serial-decoder")
				),
				new NamedChannelHandler(
						config.getInstanceName() + "-tinyos-serial-encoder",
						new TinyOsSerialEncoder(config.getInstanceName() + "-tinyos-serial-encoder")
				)
		);
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
