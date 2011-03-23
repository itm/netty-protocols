package de.uniluebeck.itm.netty.handlerstack;

import java.util.List;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;

import de.uniluebeck.itm.tr.util.Tuple;

class HandlerStackPipelineFactory implements ChannelPipelineFactory {
    private final List<Tuple<String, ChannelHandler>> handlerStack;

    HandlerStackPipelineFactory(List<Tuple<String,ChannelHandler>> handlerStack) {
        this.handlerStack = handlerStack;
    }

    public ChannelPipeline getPipeline() throws Exception {
        DefaultChannelPipeline pipeline = new DefaultChannelPipeline();

        for (Tuple<String, ChannelHandler> entry : handlerStack) {
            pipeline.addLast(entry.getFirst(), entry.getSecond());
        }

        return pipeline;
    }
}
