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

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;

import com.google.common.collect.Multimap;

public class HandlerFactoryRegistry {
    private Map<String, HandlerFactory> moduleFactories = new HashMap<String, HandlerFactory>();

    public void register(HandlerFactory factory) throws Exception {

        if (moduleFactories.containsKey(factory.getName()))
            throw new Exception("Factory of name " + factory.getName() + " already exists.");

        moduleFactories.put(factory.getName(), factory);

    }

    public ChannelHandler create(String instanceName, String factoryName, Multimap<String, String> properties) throws Exception {

        if (!moduleFactories.containsKey(factoryName))
            throw new Exception("Factory of name " + factoryName + " unknown. " + this.toString());

        return moduleFactories.get(factoryName).create(instanceName, properties);
    }

    public Map<String, String> getNameAndDescriptions() {
        Map<String, String> nameAndDescription = new HashMap<String, String>();

        for (HandlerFactory factory : moduleFactories.values())
            nameAndDescription.put(factory.getName(), factory.getDescription());

        return nameAndDescription;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Known factories: ");
        
        for (Map.Entry<String, String> entry : getNameAndDescriptions().entrySet()) {
            b.append(entry.getKey());
            b.append(" ");
        }

        return b.toString();
    }

}
