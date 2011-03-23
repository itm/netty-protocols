package de.uniluebeck.itm.netty.handlerstack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;

import de.uniluebeck.itm.tr.util.Tuple;

public class HandlerStack {
    private static final Random random = new Random();
    private ChannelHandler leftHandler;
    private ChannelHandler rightHandler;
    private Channel serverChannel;
    private Channel clientChannel;

    public void setHandlerStack(List<Tuple<String, ChannelHandler>> handlerStack) {
        final int randomId = (int) (random.nextInt() + (System.currentTimeMillis() / 1000));

        checkNotNull(leftHandler, "Left handler is null, use setLeftHandler to set it.");
        checkNotNull(getRightHandler(), "Right handler is null, use setRightHandler to set it.");

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

}
