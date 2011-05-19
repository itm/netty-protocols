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
package de.uniluebeck.itm.netty.handlerstack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.uniluebeck.itm.tr.util.Tuple;

/**
 * A stack of handlers that can easily be replaced at runtime to serve, e.g., as a filter for messages. Use the setters
 * to configure the stack. After changing something, call {@see #performSetup()} for the changes to take effect.
 * 
 * The set of handlers that acts as a filter chain is set using {@see #setHandlerStack(List)}. A convenience method
 * {@see #createFromXMLConfigurationFile(File, HandlerFactoryRegistry)} is provided to load the stack definition from a
 * file.
 * 
 * To receive data from the stack and to feed data into it, you must provide two {@see ChannelHandler} instances. One
 * for the "left" and one for the "right" side of the stack. The semantics are as follows:
 * 
 * <ul>
 * 
 * <li>The "left" handler receives the output from the top of the stack and if the handler send messages, they are
 * pushed into the stack from the top.</li>
 * 
 * <li>The "right" handler receives messages from the bottom of the stack. If the handler sends messages, they are
 * pushed into the stack from the bottom.</li>
 * 
 * </ul>
 * 
 * You can replace the handlers that filter messages at any time by calling {@see #setHandlerStack(List)} and {@see
 * #performSetup()}.
 * 
 * @author dp
 * 
 */
public class HandlerStack {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HandlerStack.class);
    private static final Random random = new Random();
    private ChannelHandler leftHandler;
    private ChannelHandler rightHandler;
    private Channel serverChannel;
    private Channel clientChannel;
    private List<Tuple<String, ChannelHandler>> handlerStack;

    public void performSetup() {
        final int randomId = (int) (random.nextInt() + (System.currentTimeMillis() / 1000));

        checkNotNull(leftHandler, "Left handler is null, use setLeftHandler to set it.");
        checkNotNull(rightHandler, "Right handler is null, use setRightHandler to set it.");

        // Disconnect existing clients/servers
        if (serverChannel != null) {
            serverChannel.disconnect().awaitUninterruptibly();
        }

        if (clientChannel != null) {
            clientChannel.disconnect().awaitUninterruptibly();
        }

        // Modify the handler stack and add the leftHandler on top
        List<Tuple<String, ChannelHandler>> modifiedHandlerStack = new LinkedList<Tuple<String, ChannelHandler>>();
        modifiedHandlerStack.add(new Tuple<String, ChannelHandler>("lefthandler", leftHandler));
        modifiedHandlerStack.addAll(handlerStack);

        // Create the new local server with the new handler pipeline
        ServerBootstrap server = new ServerBootstrap(new DefaultLocalServerChannelFactory());
        HandlerStackPipelineFactory pipelineFactory = new HandlerStackPipelineFactory(modifiedHandlerStack);
        server.setPipelineFactory(pipelineFactory);
        serverChannel = server.bind(new LocalAddress(randomId));

        // Connect the client to it
        ClientBootstrap client = new ClientBootstrap(new DefaultLocalClientChannelFactory());
        client.getPipeline().addLast("righthandler", getRightHandler());
        clientChannel = client.connect(new LocalAddress(randomId)).awaitUninterruptibly().getChannel();
    }

    public void setHandlerStack(List<Tuple<String, ChannelHandler>> handlerStack) {
        this.handlerStack = handlerStack;
    }

    public void setLeftHandler(ChannelHandler leftHandler) {
        this.leftHandler = leftHandler;
    }

    public ChannelHandler getLeftHandler() {
        return leftHandler;
    }

    public void setRightHandler(ChannelHandler rightHandler) {
        this.rightHandler = rightHandler;
    }

    public ChannelHandler getRightHandler() {
        return rightHandler;
    }

    /**
     * Loads the XML configuration file and returns a list of (handler-name, factory-name, (key,value)-pairs). The
     * format is as follows:
     * 
     * <pre>
     * <?xml version="1.0" encoding="UTF-8" ?>
     * 
     * <itm-netty-handlerstack>
     * 
     *  <handler name="time-handler" factory="movedetect-time-protocol-handler"/>
     * 
     *  <handler name="time-decoder" factory="movedetect-time-protocol-decoder">
     *          <option key="1" value="1"/> 
     *          <option key="2" value="2"/> 
     *  </handler>
     * 
     *  <handler name="time-encoder" factory="movedetect-time-protocol-encoder">
     *          <option key="11" value="11"/> 
     *          <option key="22" value="22"/> 
     *  </handler>
     * 
     * </itm-netty-handlerstack>
     * </pre>
     * 
     * @param configFile
     *            The configuration file to read.
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static List<Tuple<String, ChannelHandler>> createFromXMLConfigurationFile(File configFile,
            HandlerFactoryRegistry registry) throws Exception, IOException {

        List<Tuple<String, ChannelHandler>> handlerStack = new LinkedList<Tuple<String, ChannelHandler>>();

        if (!configFile.exists())
            throw new FileNotFoundException("Configuration file " + configFile + " not found.");

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
                    log.debug("Option for handler {}: {} = {}", new Object[] { handlerName, optionKey, optionValue });
                    options.put(optionKey, optionValue);
                }
            }

            List<Tuple<String, ChannelHandler>> channelHandlers = registry.create(handlerName, factoryName, options);
            handlerStack.addAll(channelHandlers);
        }

        // Debug output
        if (log.isDebugEnabled()) {
            log.debug("Instantiated new handler chain:");
            for (Tuple<String, ChannelHandler> entry : handlerStack)
                log.debug("Handler: {} [{}]", entry.getFirst(), entry.getSecond());
        }

        return handlerStack;
    }

}
