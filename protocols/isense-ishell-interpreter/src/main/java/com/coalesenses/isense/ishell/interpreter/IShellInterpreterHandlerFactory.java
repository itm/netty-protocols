package com.coalesenses.isense.ishell.interpreter;

import org.jboss.netty.channel.ChannelHandler;

import com.google.common.collect.Multimap;

import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;

public class IShellInterpreterHandlerFactory implements HandlerFactory{
    @Override
    public String getName() {
        return "isense-ishell-interpreter";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ChannelHandler create(Multimap<String, String> properties) throws Exception {
        return new IShellInterpreterHandler(); 
    }

    @Override
    public ChannelHandler create(String instanceName, Multimap<String, String> properties) throws Exception {
        return new IShellInterpreterHandler(instanceName); 
    }
}
