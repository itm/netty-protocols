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
package de.uniluebeck.itm.nettyprotocols;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;

import javax.annotation.Nullable;
import java.util.List;

public interface HandlerFactory {

	/**
	 * Creates a(n) instance(s) of {@link ChannelHandler}s using the given configuration options in {@code properties}.
	 *
	 * @param properties
	 * 		the configuration options for the individual {@link ChannelHandler} instances
	 * @param instanceName
	 * 		the name of the instance to be constructed
	 *
	 * @return a {@link List} of {@link Tuple} types containing instance names and the associated {@link ChannelHandler}
	 *         instances.
	 *
	 * @throws Exception
	 * 		if one or more of the {@link ChannelHandler}s could not be constructed due to e.g., configuration
	 * 		options are missing or contain invalid values
	 */
	List<Tuple<String, ChannelHandler>> create(@Nullable String instanceName, Multimap<String, String> properties)
			throws Exception;

	/**
	 * Same as calling {@link HandlerFactory#create(String, com.google.common.collect.Multimap)} with {@code
	 * instanceName=null}.
	 */
	List<Tuple<String, ChannelHandler>> create(Multimap<String, String> properties) throws Exception;

	/**
	 * Returns a {@link Multimap} containing configuration option names as keys and a human readable description of the
	 * keys as values.
	 *
	 * @return a {@link Multimap} containing configuration option names as keys and a human readable description of the
	 *         keys as values
	 */
	Multimap<String, String> getConfigurationOptions();

	/**
	 * Returns a human readable description of the handler.
	 *
	 * @return a human readable description of the handler
	 */
	String getDescription();

	/**
	 * Returns the name of the HandlerFactory. This name usually corresponds to the type of handler produced by the
	 * factory
	 * e.g., a {@link HandlerFactory} by the name of "base64-encoder" would typically produce {@link ChannelHandler}s of
	 * type {@link org.jboss.netty.handler.codec.base64.Base64Encoder}.
	 *
	 * @return the name of the HandlerFactory
	 */
	String getName();
}
