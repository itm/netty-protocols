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
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.isense.ISensePacketType;
import de.uniluebeck.itm.netty.handlerstack.isenseotap.ISenseOtapAutomatedProgrammingRequest;
import de.uniluebeck.itm.netty.handlerstack.protocolcollection.ProtocolCollection;
import de.uniluebeck.itm.netty.handlerstack.util.HeaderAndJavaBeansXMLDecoderEncoder;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.wisebed.cmdlineclient.BeanShellHelper;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClient;
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClientListener;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.WSNAsyncWrapper;
import eu.wisebed.api.common.KeyValuePair;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.controller.RequestStatus;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.testbed.api.wsn.WSNServiceHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

public class TestbedMain {
    private final static Logger log = Logger.getLogger(TestbedMain.class);


    private static final Options options = new Options();
    private static int OtapChannel = 12;
    private static String secretReservationKeys;
    private static String pccHost;
    private static int pccPort;
    private static String sessionManagementEndpointURL;
    private static SessionManagement sessionManagement = null;
    private static WSNAsyncWrapper wsn;
    private static ProtobufControllerClient pcc;

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
        log.getRootLogger().removeAllAppenders();
        log.getRootLogger().addAppender(appender);
        log.getRootLogger().setLevel(Level.DEBUG);
    }

    public static void main(String[] args) {
        configureLoggingDefaults();

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

                secretReservationKeys = properties.getProperty("testbed.secretreservationkeys");


                pccHost = properties.getProperty("testbed.protobuf.hostname");
                pccPort = Integer.parseInt(properties.getProperty("testbed.protobuf.port"));

                sessionManagementEndpointURL = properties.getProperty("testbed.sm.endpointurl");

                sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);

                connectToRuntime();


            } else {
                throw new Exception("Invalid command line arguments");
            }

        } catch (Exception e) {
            log.error("Invalid command line: " + e, e);
            usage(options);
        }

        ChannelGroup allChannels = new DefaultChannelGroup();

        final ExecutorService executorService = Executors.newCachedThreadPool();
        ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(executorService));

        final Set<Integer> otapDevices = Sets.newHashSet();


        String[] devices = nodeids.split(",");
        for (String mac : devices) {
            otapDevices.add(Integer.parseInt(mac.substring(2), 16));
            log.info("added " + mac);
        }

        byte[] otapProgram = new byte[0];
        try {
            otapProgram = Files.toByteArray(new File(binaryPath));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final byte[] finalOtapProgram = otapProgram;


        List<String> nodes = new ArrayList<String>();
        nodes.add("urn:wisebed:ctitestbed:0x8978");

        try {
            wsn.setChannelPipeline(nodes, create(xmlConfigFile), 20, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }


        wsn.resetNodes(nodes, 60, TimeUnit.SECONDS);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //packet for changing channel
        ChannelBuffer buffer = ChannelBuffers.buffer(3);
        buffer.writeByte(ISensePacketType.CODE.getValue());
        buffer.writeByte(IShellInterpreterPacketTypes.COMMAND_SET_CHANNEL.getValue());
        buffer.writeByte(OtapChannel);
        log.info("setting channel to " + OtapChannel);

        Message msg1 = new Message();
        msg1.setBinaryData(buffer.array());
        msg1.setSourceNodeId("urn:wisebed:ctitestbed:0xffff");
        try {
            msg1.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance()));
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        wsn.send(nodes, msg1, 120, TimeUnit.SECONDS);


        //packet for flashing
        ISenseOtapAutomatedProgrammingRequest req =
                new ISenseOtapAutomatedProgrammingRequest(otapDevices, finalOtapProgram);
        ChannelBuffer serializedReq = HeaderAndJavaBeansXMLDecoderEncoder.encode(ISenseOtapAutomatedProgrammingRequest.SERIALIZATION_HEADER, req);

        Message msg = new Message();
        //BinaryMessage bmsg = new BinaryMessage();
        msg.setBinaryData(serializedReq.array());
        msg.setSourceNodeId("urn:wisebed:ctitestbed:0xffff");
        try {
            msg.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance()));
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //wsn.send(nodes, msg, 120, TimeUnit.SECONDS);


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
            } catch (IOException ignore) {
            }
        }

        pcc.disconnect();
    }


    static void connectToRuntime() {

        String wsnEndpointURL = null;
        final SessionManagement sessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpointURL);
        try {
            wsnEndpointURL = sessionManagement.getInstance(BeanShellHelper.parseSecretReservationKeys(secretReservationKeys), "NONE");
        } catch (final ExperimentNotRunningException_Exception e) {
            log.error("Reservation time interval lies in the past");
            System.exit(0);
        } catch (final UnknownReservationIdException_Exception e) {
            log.error("Unknown Reservation Key");
            System.exit(0);
        }
        log.info("Got a WSN instance URL, endpoint is: " + wsnEndpointURL);

        final WSN wsnService = WSNServiceHelper.getWSNService(wsnEndpointURL);
        wsn = WSNAsyncWrapper.of(wsnService);

        pcc = ProtobufControllerClient.create(pccHost, pccPort, BeanShellHelper.parseSecretReservationKeys(secretReservationKeys));
        pcc.connect();

        pcc.addListener(new ControllerClientListener());
    }


    private static List<ChannelHandlerConfiguration> create(final File configFile) throws Exception {

        List<ChannelHandlerConfiguration> channelHandlerConfigurations = newArrayList();

        if (!configFile.exists()) {
            throw new FileNotFoundException("Configuration file " + configFile + " not found.");
        }

        XMLConfiguration config = new XMLConfiguration(configFile);

        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> handlers = config.configurationsAt("handler");
        for (HierarchicalConfiguration sub : handlers) {

            final String factoryName = sub.getString("[@factory]");

            final ChannelHandlerConfiguration chc = new ChannelHandlerConfiguration();
            chc.setName(factoryName);

            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> xmlOptions = sub.configurationsAt("option");

            for (HierarchicalConfiguration xmlOption : xmlOptions) {

                String optionKey = xmlOption.getString("[@key]");
                String optionValue = xmlOption.getString("[@value]");

                if (optionKey != null && optionValue != null && !"".equals(optionKey) && !"".equals(optionValue)) {
                    log.debug("Option for handler " + factoryName + ": " + optionKey + " = " + optionValue);
                    final KeyValuePair pair = new KeyValuePair();
                    pair.setKey(optionKey);
                    pair.setValue(optionValue);
                    chc.getConfiguration().add(pair);
                }
            }

            channelHandlerConfigurations.add(chc);
        }

        return channelHandlerConfigurations;
    }


    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(120, Main.class.getCanonicalName(), null, options, null);
        System.exit(1);
    }


    private static class ControllerClientListener implements ProtobufControllerClientListener {

        @Override
        public void onConnectionClosed() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onConnectionEstablished() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void experimentEnded() {
            log.info("Experiment Ended");
        }

        @Override
        public void receive(List<Message> messages) {
            for (int i = 0; i < messages.size(); i++) {
                Message msg = (Message) messages.get(i);
                synchronized (System.out) {
                    String s = BeanShellHelper.toString(msg, true);
                    log.debug(s);
                }
            }

        }

        @Override
        public void receiveNotification(List<String> strings) {
            log.info("received " + strings.size());
        }

        @Override
        public void receiveStatus(List<RequestStatus> requestStatuses) {
            for (int i = 0; i < requestStatuses.size(); i++) {
                RequestStatus msg = (RequestStatus) requestStatuses.get(i);
                synchronized (System.out) {
                    String s = msg.toString();
                    log.debug(s);
                }
            }
        }
    }
}
