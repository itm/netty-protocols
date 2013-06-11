package de.uniluebeck.itm.nettyprotocols;

import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Set;

class HandlerFactoryMapImpl extends HashMap<String, HandlerFactory> implements HandlerFactoryMap {

	@Inject
	public HandlerFactoryMapImpl(final Set<HandlerFactory> handlerFactories) {
		for (HandlerFactory handlerFactory : handlerFactories) {
			put(handlerFactory.getName(), handlerFactory);
		}
	}


}
