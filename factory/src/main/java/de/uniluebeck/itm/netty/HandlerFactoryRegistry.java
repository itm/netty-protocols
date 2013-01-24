/**
 * Copyright (c) 2010, Daniel Bimschas and Dennis Pfisterer, Institute of Telematics, University of Luebeck
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uniluebeck.itm.netty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class HandlerFactoryRegistry {

	private static final Logger log = LoggerFactory.getLogger(HandlerFactoryRegistry.class);

	@SuppressWarnings("unused")
	public static class ChannelHandlerDescription {

		private final Multimap<String, String> configurationOptions;

		private final String description;

		private final String name;

		private ChannelHandlerDescription(final String name, final String description,
										  final Multimap<String, String> configurationOptions) {
			this.configurationOptions = configurationOptions;
			this.description = description;
			this.name = name;
		}

		public Multimap<String, String> getConfigurationOptions() {
			return configurationOptions;
		}

		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "ChannelHandlerDescription{" +
					"name='" + name + '\'' +
					", description='" + description + '\'' +
					", configurationOptions=" + configurationOptions +
					'}';
		}
	}

	private Map<String, HandlerFactory> moduleFactories = newHashMap();

	public void register(final HandlerFactory factory) throws Exception {

		if (moduleFactories.containsKey(factory.getName())) {
			throw new Exception("Factory of name " + factory.getName() + " already exists.");
		}

		moduleFactories.put(factory.getName(), factory);

	}

	/**
	 * Loads the XML configuration file and returns a list of (handler-name, factory-name, (key,value)-pairs). The format
	 * is as follows:
	 * <p/>
	 * <pre>
	 * {@code
	 *
	 * <?xml version="1.0" encoding="UTF-8" ?>
	 * <itm-netty-handlerstack>
	 *   <handler name="time-handler" factory="movedetect-time-protocol-handler"/>
	 *   <handler name="time-decoder" factory="movedetect-time-protocol-decoder">
	 *     <option key="1" value="1"/>
	 *     <option key="2" value="2"/>
	 *   </handler>
	 *   <handler name="time-encoder" factory="movedetect-time-protocol-encoder">
	 *     <option key="11" value="11"/>
	 *     <option key="22" value="22"/>
	 *   </handler>
	 * </itm-netty-handlerstack>
	 * }
	 * </pre>
	 *
	 * @param configFile The configuration file to read.
	 *
	 * @return
	 *
	 * @throws Exception
	 */
	public List<Tuple<String, ChannelHandler>> create(final File configFile) throws Exception {

		LinkedList<Tuple<String, ChannelHandler>> handlerStack = new LinkedList<Tuple<String, ChannelHandler>>();

		if (!configFile.exists()) {
			throw new FileNotFoundException("Configuration file " + configFile + " not found.");
		}

		XMLConfiguration config = new XMLConfiguration(configFile);

		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> handlers = config.configurationsAt("handler");
		for (HierarchicalConfiguration sub : handlers) {
			String factoryName = sub.getString("[@factory]");
			String handlerName = sub.getString("[@name]");
			log.debug("Handler {} of factory type {}", handlerName, factoryName);

			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> xmlOptions = sub.configurationsAt("option");
			Multimap<String, String> options = ArrayListMultimap.create();

			for (HierarchicalConfiguration xmlOption : xmlOptions) {

				String optionKey = xmlOption.getString("[@key]");
				String optionValue = xmlOption.getString("[@value]");

				if (optionKey != null && optionValue != null && !"".equals(optionKey) && !"".equals(optionValue)) {
					log.debug("Option for handler {}: {} = {}", new Object[]{handlerName, optionKey, optionValue});
					options.put(optionKey, optionValue);
				}
			}

			List<Tuple<String, ChannelHandler>> channelHandlers = create(handlerName, factoryName, options);
			for (Tuple<String, ChannelHandler> channelHandler : channelHandlers) {
				handlerStack.addFirst(channelHandler);
			}
		}

		// Debug output
		if (log.isDebugEnabled()) {
			log.debug("Instantiated new handler chain:");
			for (Tuple<String, ChannelHandler> entry : handlerStack) {
				log.debug("Handler: {} [{}]", entry.getFirst(), entry.getSecond());
			}
		}

		return handlerStack;
	}

	public List<Tuple<String, ChannelHandler>> create(
			final List<Tuple<String, Multimap<String, String>>> channelHandlerConfigurations) throws Exception {

		List<Tuple<String, ChannelHandler>> channelHandlers = newArrayList();

		int layer = 0;
		for (Tuple<String, Multimap<String, String>> channelHandlerConfiguration : channelHandlerConfigurations) {

			final String factoryName = channelHandlerConfiguration.getFirst();
			final List<Tuple<String, ChannelHandler>> list = create(
					layer + "-" + factoryName,
					factoryName,
					channelHandlerConfiguration.getSecond()
			);

			channelHandlers.addAll(list);
			layer++;
		}

		return channelHandlers;
	}

	public List<Tuple<String, ChannelHandler>> create(final String instanceName, final String factoryName,
													  final Multimap<String, String> properties) throws Exception {

		if (!moduleFactories.containsKey(factoryName)) {
			throw new Exception("Factory of name " + factoryName + " unknown. " + this.toString());
		}

		return moduleFactories.get(factoryName).create(instanceName, properties);
	}

	public List<ChannelHandlerDescription> getChannelHandlerDescriptions() {

		List<ChannelHandlerDescription> handlers = newArrayList();

		for (HandlerFactory handlerFactory : moduleFactories.values()) {
			handlers.add(new ChannelHandlerDescription(
					handlerFactory.getName(),
					handlerFactory.getDescription(),
					handlerFactory.getConfigurationOptions()
			)
			);
		}

		return handlers;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Known factories: ");

		for (ChannelHandlerDescription chd : getChannelHandlerDescriptions()) {
			b.append(chd);
			b.append(" ");
		}

		return b.toString();
	}

}
