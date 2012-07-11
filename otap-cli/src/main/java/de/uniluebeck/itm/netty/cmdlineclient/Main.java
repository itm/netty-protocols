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
package de.uniluebeck.itm.netty.cmdlineclient;

import com.coalesenses.isense.ishell.interpreter.IShellInterpreterPacketTypes;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import de.uniluebeck.itm.netty.handlerstack.FilterPipeline;
import de.uniluebeck.itm.netty.handlerstack.FilterPipelineImpl;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketType;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapAutomatedProgrammingRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectControlStop;
import de.uniluebeck.itm.netty.handlerstack.protocolcollection.ProtocolCollection;
import de.uniluebeck.itm.netty.handlerstack.util.HandlerTools;
import de.uniluebeck.itm.netty.handlerstack.util.HeaderAndJavaBeansXMLDecoderEncoder;
import de.uniluebeck.itm.wsn.drivers.core.Device;
import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactory;
import de.uniluebeck.itm.wsn.drivers.factories.DeviceFactoryModule;
import de.uniluebeck.itm.wsn.drivers.factories.DeviceType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.iostream.IOStreamAddress;
import org.jboss.netty.channel.iostream.IOStreamChannelFactory;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class Main {


	private static final Options options = new Options();

	private static int OtapChannel = 12;

	static {
		options.addOption("p", "properties", true, "my.properties");
		options.addOption("d", "device", true, "Device address (e.g., /dev/ttyUSB1)");
		options.addOption("i", "isense", true, "isense macs (e.g. 0x1234,0x1235");
		options.addOption("b", "binary", true, "binary file (e.g. DemoApp.bin");
		options.addOption("f", "file", true, "A config file");
		options.addOption("c", "channel", true, "The channel to search in");
		options.addOption("v", "verbose", false, "Verbose logging output");
		options.addOption("x", "xtremlyverbose", false, "Extremly verbose logging output");
		options.addOption("h", "help", false, "Help output");
	}

	private static void configureLoggingDefaults() {
		PatternLayout patternLayout = new PatternLayout("%-13d{HH:mm:ss,SSS} | %-25.25c{2} | %-5p | %m%n");

		final Appender appender = new ConsoleAppender(patternLayout);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	public static void main(String[] args) {
		configureLoggingDefaults();
		final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);

		// Create a handler factory and populate it with all MOVEDETECT factories
		HandlerFactoryRegistry factoryRegistry = new HandlerFactoryRegistry();
		ProtocolCollection.registerProtocols(factoryRegistry);

		// Options set from the command line
		String deviceAddress = null;
		String nodeids = null;
		String binaryPath = null;

		File xmlConfigFile = null;


		// Parse the command line
		try {
			CommandLine line = new PosixParser().parse(options, args);

			Logger.getRootLogger().setLevel(Level.INFO);

			// Check if verbose output should be used
			if (line.hasOption('v')) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
			}

			// Check if xtremlyverbose output should be used
			if (line.hasOption('x')) {
				Logger.getRootLogger().setLevel(Level.TRACE);
			}

			// Output help and exit
			if (line.hasOption('h')) {
				usage(options);
			}

			// Check if verbose output should be used
			if (line.hasOption('p')) {
				Properties properties = new Properties();
				properties.load(new FileInputStream(line.getOptionValue("p")));
				log.info("loaded properties");
				deviceAddress = properties.getProperty("device");
				nodeids = properties.getProperty("nodeids");
				binaryPath = properties.getProperty("binary");
				OtapChannel = Integer.parseInt(properties.getProperty("channel"));
				xmlConfigFile = new File(properties.getProperty("xmlconfig"));


			} else {


				if (line.hasOption('d')) {
					deviceAddress = line.getOptionValue('d');
				} else {
					throw new Exception("Please supply -d");
				}

				if (line.hasOption('f')) {
					xmlConfigFile = new File(line.getOptionValue('f'));
				} else {
					throw new Exception("Please supply -f");
				}


				if (line.hasOption('i')) {
					nodeids = line.getOptionValue('i');
				} else {
					throw new Exception("Please supply -i");
				}

				if (line.hasOption('b')) {
					binaryPath = line.getOptionValue('b');
				} else {
					throw new Exception("Please supply -b");
				}
				if (line.hasOption('c')) {
					OtapChannel = Integer.parseInt(line.getOptionValue('c'));
				} else {
					OtapChannel = 12;
				}
			}

		} catch (Exception e) {
			log.error("Invalid command line: " + e, e);
			usage(options);
		}

		ChannelGroup allChannels = new DefaultChannelGroup();

		final ExecutorService executorService = Executors.newCachedThreadPool();

		final Set<Integer> otapDevices = Sets.newHashSet();


		String[] devices = nodeids.split(",");
		for (String mac : devices) {
			otapDevices.add(Integer.parseInt(mac.substring(2), 16));
			log.info("added {}({}) ", mac, Integer.parseInt(mac.substring(2), 16));
		}

		byte[] otapProgram = new byte[0];
		try {
			otapProgram = Files.toByteArray(new File(binaryPath));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		final byte[] finalOtapProgram = otapProgram;
		final SimpleChannelHandler otapProgrammingHandler = new SimpleChannelHandler() {
			/*
						 * (non-Javadoc)
						 *
						 * @see
						 * org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext
						 * , org.jboss.netty.channel.ChannelStateEvent)
						 */
			@Override
			public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
				log.debug("----otapProgrammingHandler:connected----");
				try {
					super.channelConnected(ctx, e);
				} catch (Exception e1) {
					e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
				executorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							log.debug(" :" + e1, e1);
						}


//                        IShellInterpreterSetChannelMessage setChannelMessage = new IShellInterpreterSetChannelMessage((byte) channelNumber);
						ChannelBuffer buffer = ChannelBuffers.buffer(3);
						buffer.writeByte(ISensePacketType.CODE.getValue());
						buffer.writeByte(IShellInterpreterPacketTypes.COMMAND_SET_CHANNEL.getValue());
						buffer.writeByte(OtapChannel);

						log.debug("Setting channel to {}", OtapChannel);
						e.getChannel().write(buffer);


						ISenseOtapAutomatedProgrammingRequest req =
								new ISenseOtapAutomatedProgrammingRequest(otapDevices, finalOtapProgram);


						e.getChannel().write(HeaderAndJavaBeansXMLDecoderEncoder
								.encode(ISenseOtapAutomatedProgrammingRequest.SERIALIZATION_HEADER, req)
						);

						log.debug("sent otap");
					}
				}
				);
			}

			/*
						* (non-Javadoc)
						*
						* @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.
						* ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
						*/
			@Override
			public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
				HandlerTools.sendDownstream(new PresenceDetectControlStop(), ctx);
				super.channelDisconnected(ctx, e);
			}

		};


		final FilterPipeline filterPipeline = new FilterPipelineImpl();
		try {
			filterPipeline.setChannelPipeline(factoryRegistry.create(xmlConfigFile));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		final DeviceFactory deviceFactory = Guice
				.createInjector(new DeviceFactoryModule())
				.getInstance(DeviceFactory.class);

		final Device device = deviceFactory.create(executorService, DeviceType.ISENSE);

		try {
			device.connect(deviceAddress);
		} catch (IOException e) {
			throw new RuntimeException("Connection to device at port \"" + args[1] + "\" could not be established!");
		}
		if (!device.isConnected()) {
			throw new RuntimeException("Connection to device at port \"" + args[1] + "\" could not be established!");
		}

		final InputStream inputStream = device.getInputStream();
		final OutputStream outputStream = device.getOutputStream();

		final ClientBootstrap bootstrap = new ClientBootstrap(new IOStreamChannelFactory(executorService));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				final ChannelPipeline pipeline = pipeline();
				pipeline.addLast("filterPipeline", filterPipeline);
				log.debug(pipeline.toString());
				return pipeline;
			}
		}
		);
		final ChannelFuture connectFuture = bootstrap.connect(new IOStreamAddress(inputStream, outputStream));
		final Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		allChannels.add(channel);

		// Run the program until the user enters "exit".
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.err.println("Enter 'exit' to exit.");

		boolean exit = false;
		while (!exit) {
			try {
				String line = reader.readLine();
				if ("exit".equals(line)) {
					exit = true;
				} else if ("send".equals(line)) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						log.debug(" :" + e1, e1);
					}


//                        IShellInterpreterSetChannelMessage setChannelMessage = new IShellInterpreterSetChannelMessage((byte) channelNumber);
					ChannelBuffer buffer = ChannelBuffers.buffer(3);
					buffer.writeByte(ISensePacketType.CODE.getValue());
					buffer.writeByte(IShellInterpreterPacketTypes.COMMAND_SET_CHANNEL.getValue());
					buffer.writeByte(OtapChannel);

					log.debug("Setting channel to {}", OtapChannel);
					allChannels.write(buffer);


					ISenseOtapAutomatedProgrammingRequest req =
							new ISenseOtapAutomatedProgrammingRequest(otapDevices, finalOtapProgram);


					allChannels.write(HeaderAndJavaBeansXMLDecoderEncoder
							.encode(ISenseOtapAutomatedProgrammingRequest.SERIALIZATION_HEADER, req)
					);

					log.debug("sent otap");
				}
			} catch (IOException ignore) {
			}
		}

		// Close the connection.
		allChannels.close().awaitUninterruptibly();

		// Shut down all thread pools to exit.
		bootstrap.releaseExternalResources();
		System.exit(0);
	}

	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(120, Main.class.getCanonicalName(), null, options, null);
		System.exit(1);
	}
}
