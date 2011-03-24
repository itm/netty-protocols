package de.uniluebeck.itm.netty.handlerstack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
     * Loads the XML configuration file and returns a list of (handler-name,
     * factory-name, (key,value)-pairs). The format is as follows:
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

            ChannelHandler channelHandler = registry.create(factoryName, options);
            handlerStack.add(new Tuple<String, ChannelHandler>(handlerName, channelHandler));
        }

        return handlerStack;
    }

}
