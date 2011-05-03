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

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;

public class ISenseOtapAutomatedHandlerFactory implements HandlerFactory {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ISenseOtapAutomatedHandlerFactory.class);

    private static final String PRESENCE_DETECT_TIMEOUT = "presenceDetectTimeout";
    private static final String PRESENCE_DETECT_TIMEOUT_TIMEUNIT = "presenceDetectTimeoutTimeUnit";

    private static final String PROGRAMMING_TIMEOUT = "programmingTimeout";
    private static final String PROGRAMMING_TIMEOUT_TIMEUNIT = "programmingTimeoutTimeUnit";

    @Override
    public String getName() {
        return "isense-otap-automated-handler";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ChannelHandler create(Multimap<String, String> properties) throws Exception {
        return create(null, properties);
    }

    @Override
    public ChannelHandler create(String instanceName, Multimap<String, String> properties) throws Exception {
        long presenceDetectTimeout = 30;
        TimeUnit presenceDetectTimeoutTimeUnit = TimeUnit.SECONDS;

        long programmingTimeout = 10;
        TimeUnit programmingTimeoutTimeUnit = TimeUnit.MINUTES;

        if (properties.containsKey(PRESENCE_DETECT_TIMEOUT))
            presenceDetectTimeout = Integer.parseInt(properties.get(PRESENCE_DETECT_TIMEOUT).iterator().next());

        if (properties.containsKey(PRESENCE_DETECT_TIMEOUT_TIMEUNIT))
            presenceDetectTimeoutTimeUnit =
                    TimeUnit.valueOf(properties.get(PRESENCE_DETECT_TIMEOUT_TIMEUNIT).iterator().next());

        if (properties.containsKey(PROGRAMMING_TIMEOUT))
            programmingTimeout = Integer.parseInt(properties.get(PROGRAMMING_TIMEOUT).iterator().next());

        if (properties.containsKey(PROGRAMMING_TIMEOUT_TIMEUNIT))
            programmingTimeoutTimeUnit =
                    TimeUnit.valueOf(properties.get(PROGRAMMING_TIMEOUT_TIMEUNIT).iterator().next());

        log.debug("Creating new Otap Automated Handler, presenceDetectTimeout: {}{}, programmingTimeout: {}{}",
                new Object[] { presenceDetectTimeout, presenceDetectTimeoutTimeUnit, programmingTimeout,
                        programmingTimeoutTimeUnit });

        return new ISenseOtapAutomatedHandler(instanceName, presenceDetectTimeout, presenceDetectTimeoutTimeUnit, programmingTimeout,
                programmingTimeoutTimeUnit);
    }
}
