package de.uniluebeck.itm.netty.handlerstack;

import java.util.List;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.tr.util.Tuple;

class HandlerStackPipelineFactory implements ChannelPipelineFactory {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HandlerStackPipelineFactory.class);

    private final List<Tuple<String, ChannelHandler>> handlerStack;

    HandlerStackPipelineFactory(final List<Tuple<String, ChannelHandler>> handlerStack) {
        this.handlerStack = handlerStack;
    }

    public ChannelPipeline getPipeline() throws Exception {
        DefaultChannelPipeline pipeline = new DefaultChannelPipeline();

        log.debug("Creating new pipeline");
        for (Tuple<String, ChannelHandler> entry : handlerStack) {
            log.debug("- New (first) handler: {}", entry.getFirst());
            pipeline.addFirst(entry.getFirst(), entry.getSecond());

        }

        return pipeline;
    }
}
