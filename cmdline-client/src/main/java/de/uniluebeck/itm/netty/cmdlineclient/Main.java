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

import com.coalesenses.isense.ishell.interpreter.IShellInterpreterSetChannelMessage;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import de.uniluebeck.itm.netty.handlerstack.FilterHandler;
import de.uniluebeck.itm.netty.handlerstack.FilterPipeline;
import de.uniluebeck.itm.netty.handlerstack.FilterPipelineImpl;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapAutomatedProgrammingRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.presencedetect.PresenceDetectControlStop;
import de.uniluebeck.itm.netty.handlerstack.protocolcollection.ProtocolCollection;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class Main {
    static final Options options = new Options();
    static {
        options.addOption("d", "device", true, "Device address (e.g., /dev/ttyUSB0)");
        options.addOption("f", "file", true, "A config file");
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

    public static void main(String[] args) throws Exception {
        configureLoggingDefaults();
        final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);

        // Create a handler factory and populate it with all MOVEDETECT factories
        HandlerFactoryRegistry factoryRegistry = new HandlerFactoryRegistry();
        ProtocolCollection.registerProtocols(factoryRegistry);

        // Options set from the command line
        String deviceAddress = null;
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

        } catch (Exception e) {
            log.error("Invalid command line: " + e, e);
            usage(options);
        }

        ChannelGroup allChannels = new DefaultChannelGroup();

        final ExecutorService executorService = Executors.newCachedThreadPool();
        ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(executorService));

        final Set<Integer> otapDevices = Sets.newHashSet();

        @SuppressWarnings("unused")
        final int[] wisebedISenseDevices =
                new int[] { 0x1bb3, 0x99a8, 0x997e, 0x14e2, 0x1cca, 0xf85d, 0x5a34, 0xcf04, 0xcc33, 0x151f, 0x1c6c,
                        0x1c73, 0x61e1, 0xf7b7, 0x61e5, 0x1c2c, 0x1bd8, 0x1bb0, 0xcc3a, 0x85a4, 0x80f5, 0x599d, 0xcbe4,
                        0xf859, 0x99af, 0x753d, 0x1b57, 0x5a35, 0x1b74, 0xcc3d, 0x970b, 0x1b5a, 0x1b6b, 0x1c72, 0xf851,
                        0xcff1, 0x1cd2, 0x7e6c, 0xcc43, 0x85ba, 0x9960, 0x9961, 0x14f7, 0x96f9, 0xc179, 0x96df, 0x9995,
                        0x971e, 0xcbe5, 0x1234, 0x14e0, 0x96f0, 0x1721, 0x5980 };

        // for (Integer id : wisebedISenseDevices)
        // otapDevices.add(id);
        otapDevices.add(0x1b87);

        final byte[] otapProgram = Files.toByteArray(new File("src/main/resources/iSenseDemoApp.bin"));

        final SimpleChannelHandler otapProgrammingHandler = new SimpleChannelHandler() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext
             * , org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelConnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        e.getChannel().write(new IShellInterpreterSetChannelMessage((byte) 11));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            log.debug(" :" + e1, e1);
                        }

                        ISenseOtapAutomatedProgrammingRequest req =
                                new ISenseOtapAutomatedProgrammingRequest(otapDevices, otapProgram);

                        e.getChannel().write(req);
                    }
                });

                super.channelConnected(ctx, e);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.
             * ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                e.getChannel().write(new PresenceDetectControlStop());
                super.channelDisconnected(ctx, e);
            }

        };

        FilterPipeline filterPipeline = new FilterPipelineImpl();
        filterPipeline.setChannelPipeline(factoryRegistry.create(xmlConfigFile));

        final FilterHandler filterHandler = new FilterHandler(filterPipeline);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                final ChannelPipeline pipeline = pipeline();
                pipeline.addLast("filterHandler", filterHandler);
                pipeline.addLast("otapProgrammingHandler", otapProgrammingHandler);
                return pipeline;
            }
        });

        // Create a new connection and wait until the connection is made successfully.
        ChannelFuture connectFuture = bootstrap.connect(new RXTXDeviceAddress(deviceAddress));
        Channel rxtxChannel = connectFuture.awaitUninterruptibly().getChannel();
        allChannels.add(rxtxChannel);

        // Run the program until the user enters "exit".
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("Enter 'exit' to exit.");

        boolean exit = false;
        while (!exit) {
            try {
                String line = reader.readLine();
                if ("exit".equals(line)) {
                    exit = true;
                }
            } catch (IOException e) {
                // ignore
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
