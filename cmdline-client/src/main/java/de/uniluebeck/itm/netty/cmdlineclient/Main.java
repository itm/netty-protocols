package de.uniluebeck.itm.netty.cmdlineclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.LoggerFactory;

import com.coalesenses.isense.ishell.interpreter.IShellInterpreterSetChannelMessage;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import de.uniluebeck.itm.netty.channelflange.ChannelFlange;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.HandlerStack;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.PresenceDetectControlStart;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.PresenceDetectControlStop;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.generatedmessages.OtapProgramRequest;
import de.uniluebeck.itm.netty.handlerstack.protocolcollection.ProtocolCollection;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;

public class Main {
    static final Options options = new Options();
    static {
        options.addOption("d", "device", true, "Device address (e.g., /dev/ttyUSB0)");
        options.addOption("f", "file", true, "A config file");
        options.addOption("v", "verbose", false, "Verbose logging output");
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

            // Check if verbose output should be used
            if (line.hasOption('v')) {
                Logger.getRootLogger().setLevel(Level.TRACE);
            } else {
                Logger.getRootLogger().setLevel(Level.INFO);
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

        final Set<Integer> otapDevices = Sets.newHashSet(0x1b87);
        final byte[] otapProgram = Files.toByteArray(new File("src/main/resources/iSenseDemoApp.bin"));
        
        SimpleChannelHandler leftStackHandler = new SimpleChannelHandler() {

            
            /* (non-Javadoc)
             * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelConnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
                executorService.submit(new Runnable() {
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        e.getChannel().write(new IShellInterpreterSetChannelMessage((byte) 21));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            log.debug(" :" + e1, e1);
                        }
                        e.getChannel().write(new PresenceDetectControlStart());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            log.debug(" :" + e1, e1);
                        }
                        e.getChannel().write(new PresenceDetectControlStop());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            log.debug(" :" + e1, e1);
                        }
                        e.getChannel().write(new ISenseOtapProgramRequest(otapDevices, otapProgram));
                    }
                });
                super.channelConnected(ctx, e);
            }

            /* (non-Javadoc)
             * @see org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
             */
            @Override
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                e.getChannel().write(new PresenceDetectControlStop());
                super.channelDisconnected(ctx, e);
            }

            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                /*
                 * Object messageObject = e.getMessage(); if (messageObject instanceof ChannelBuffer) { ChannelBuffer
                 * message = (ChannelBuffer) messageObject; log.info("Byte[] message received: {}",
                 * StringUtils.toHexString(((ChannelBuffer) message).array())); } else if (messageObject instanceof
                 * ISensePacket) { ISensePacket iSensePacket = (ISensePacket) messageObject;
                 * log.info("iSense packet received: " + iSensePacket); } else {
                 * log.info("Unknown packet type received"); }
                 */
                super.messageReceived(ctx, e);
            }

        };

        // Create a new connection and wait until the connection is made successfully.
        ChannelFuture connectFuture = bootstrap.connect(new RXTXDeviceAddress(deviceAddress));
        Channel rxtxChannel = connectFuture.awaitUninterruptibly().getChannel();
        allChannels.add(rxtxChannel);

        // Create a channel flange that mediates between the RXTX channel and offers a handler that can be passed to the
        // handler stack
        ChannelFlange flange = new ChannelFlange();
        flange.setRightChannel(rxtxChannel);

        // The stack of handlers that can be changed at any time
        HandlerStack stack = new HandlerStack();

        // The "left" handler (a logger)
        stack.setLeftHandler(leftStackHandler);

        // The "right" handler (connect to RXTX via a flange)
        stack.setRightHandler(flange.getLeftHandler());

        // Instantiate the handlerstack from the configuration file
        stack.setHandlerStack(HandlerStack.createFromXMLConfigurationFile(xmlConfigFile, factoryRegistry));

        // "Switch" to the new setup (must be called after using any setters to activate the new configuration)
        stack.performSetup();
        
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
