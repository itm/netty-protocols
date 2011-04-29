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
package de.uniluebeck.itm.netty.handlerstack.isenseotap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;

public class OtapHandlerFactory implements HandlerFactory {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OtapHandlerFactory.class);

    private static final String THREAD_COUNT = "threadCount";

    @Override
    public String getName() {
        return "isense-otap-handler";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ChannelHandler create(Multimap<String, String> properties) throws Exception {
        int threadCount = 10;

        log.debug("Creating new Otap Handler, threadCount: {}", new Object[] { threadCount });

        if (properties.containsKey(THREAD_COUNT))
            threadCount = Integer.parseInt(properties.get(THREAD_COUNT).iterator().next());

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadCount);

        return new OtapHandler(executorService);
    }
}
