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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactory;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryPropertiesHelper;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.init.ISenseOtapInitHandler;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectHandler;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.program.ISenseOtapProgramHandler;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ISenseOtapFactory implements HandlerFactory {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ISenseOtapFactory.class);

	private static final String PRESENCE_DETECT_INTERVAL = "presenceDetectInterval";

	private static final String DEVICE_TIMEOUT = "deviceTimeout";

	private static final String TIMEUNIT = "timeunit";

	private static final String THREAD_COUNT = "threadCount";

	private static final String MAX_REREQUESTS = "maxRerequests";

	private static final String TIMEOUT_MULTIPLIER = "timeoutMultiplier";

	@Override
	public List<Tuple<String, ChannelHandler>> create(String instanceName, Multimap<String, String> properties)
			throws Exception {
		log.debug("Creating new Otap Handler instances: {}", instanceName);

		List<Tuple<String, ChannelHandler>> handlers = new LinkedList<Tuple<String, ChannelHandler>>();

		{
			String tempInstanceName = instanceName + "-automated-handler";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					new ISenseOtapAutomatedProgrammingHandler(tempInstanceName)
			)
			);
		}
		{
			String tempInstanceName = instanceName + "-presence-detect";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					createPresenceDetect(tempInstanceName, properties)
			)
			);
		}
		{
			String tempInstanceName = instanceName + "-init";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					new ISenseOtapInitHandler(tempInstanceName)
			)
			);
		}
		{
			String tempInstanceName = instanceName + "-program";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					createOtapProgramHandler(tempInstanceName, properties)
			)
			);
		}
		{
			String tempInstanceName = instanceName + "-decoder";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					new ISenseOtapPacketDecoder(tempInstanceName)
			)
			);
		}
		{
			String tempInstanceName = instanceName + "-encoder";
			handlers.add(new Tuple<String, ChannelHandler>(
					tempInstanceName,
					new ISenseOtapPacketEncoder(tempInstanceName)
			)
			);
		}

		return handlers;
	}

	@Override
	public List<Tuple<String, ChannelHandler>> create(Multimap<String, String> properties) throws Exception {
		return create(null, properties);
	}

	@Override
	public Multimap<String, String> getConfigurationOptions() {
		final HashMultimap<String, String> map = HashMultimap.create();
		map.put(PRESENCE_DETECT_INTERVAL, "(int, optional, default=2000)");
		map.put(DEVICE_TIMEOUT, "(int, optional, default=160*" + PRESENCE_DETECT_INTERVAL + ")");
		map.put(TIMEUNIT, "(int, optional, default=" + TimeUnit.MILLISECONDS + ")");
		map.put(THREAD_COUNT, "(int, optional, default=10)");
		map.put(MAX_REREQUESTS, "(short, optional, default=30)");
		map.put(TIMEOUT_MULTIPLIER, "(short, optional, default=1000)");
		return map;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getName() {
		return "isense-otap";
	}

	private ChannelHandler createPresenceDetect(String instanceName, Multimap<String, String> properties)
			throws Exception {

		int presenceDetectInterval =
				HandlerFactoryPropertiesHelper.getFirstValueOf(properties, PRESENCE_DETECT_INTERVAL, 2000);
		int deviceTimeout = HandlerFactoryPropertiesHelper
				.getFirstValueOf(properties, DEVICE_TIMEOUT, (short) 160 * presenceDetectInterval);
		TimeUnit timeunit = HandlerFactoryPropertiesHelper.getFirstValueOf(properties, TIMEUNIT, TimeUnit.MILLISECONDS);
		int threadCount = HandlerFactoryPropertiesHelper.getFirstValueOf(properties, THREAD_COUNT, 10);

		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadCount);

		log.debug(
				"Creating new Presence Detect Handler presenceDetectInterval: {}, deviceTimeout: {}, timeunit: {}, threadCount: {}",
				new Object[]{presenceDetectInterval, deviceTimeout, timeunit, threadCount}
		);

		return new PresenceDetectHandler(instanceName, executorService, presenceDetectInterval, deviceTimeout, timeunit
		);
	}

	private ChannelHandler createOtapProgramHandler(String instanceName, Multimap<String, String> properties)
			throws Exception {

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

		short settingMaxRerequests =
				HandlerFactoryPropertiesHelper.getFirstValueOf(properties, MAX_REREQUESTS, (short) 30);

		short settingTimeoutMultiplier =
				HandlerFactoryPropertiesHelper.getFirstValueOf(properties, TIMEOUT_MULTIPLIER, (short) 1000);

		log.debug("Creating new Otap Program Handler, settingMaxRerequests: {}, settingTimeoutMultiplier: {}",
				settingMaxRerequests, settingTimeoutMultiplier
		);

		return new ISenseOtapProgramHandler(instanceName, executorService, settingMaxRerequests,
				settingTimeoutMultiplier
		);
	}

}
