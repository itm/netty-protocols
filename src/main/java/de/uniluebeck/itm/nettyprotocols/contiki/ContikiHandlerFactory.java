package de.uniluebeck.itm.nettyprotocols.contiki;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.nettyprotocols.*;

public class ContikiHandlerFactory implements HandlerFactory {

	@Override
	public NamedChannelHandlerList create(final ChannelHandlerConfig config) throws Exception {
		final NamedChannelHandlerList handlers = new NamedChannelHandlerList();
		handlers.add(new NamedChannelHandler("contiki-newline-decoder", new NewlineDecoder()));
		handlers.add(new NamedChannelHandler("contiki-newline-encoder", new NewlineEncoder()));
		return handlers;
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		return HashMultimap.create();
	}

	@Override
	public String getDescription() {
		return "A protocol stack for serial communication with the Contiki operating system. Adds LF chars "
				+ "(if necessary) when sending messages downstream towards the node (encoding), decodes the data stream "
				+ "coming from the node by splitting it up on every LF (\\r\\n or \\n) character received. If a message "
				+ "coming from the node contains more than " + NewlineDecoder.DEFAULT_MAX_FRAME_LENGTH + " characters "
				+ "it will be discarded with an error.";
	}

	@Override
	public String getName() {
		return "contiki";
	}
}
