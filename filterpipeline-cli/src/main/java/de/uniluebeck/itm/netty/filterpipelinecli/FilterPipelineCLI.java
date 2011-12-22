package de.uniluebeck.itm.netty.filterpipelinecli;

import com.google.common.base.Joiner;
import de.uniluebeck.itm.netty.handlerstack.FilterPipeline;
import de.uniluebeck.itm.netty.handlerstack.FilterPipelineImpl;
import de.uniluebeck.itm.netty.handlerstack.HandlerFactoryRegistry;
import de.uniluebeck.itm.netty.handlerstack.protocolcollection.ProtocolCollection;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;
import de.uniluebeck.itm.tr.util.Logging;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Lists.newArrayList;
import static org.jboss.netty.channel.Channels.pipeline;

public class FilterPipelineCLI {

	static {
		Logging.setLoggingDefaults();
	}

	private static final Logger log = LoggerFactory.getLogger(FilterPipelineCLI.class);

	private static final int EXIT_CODE_INVALID_ARGUMENTS = 1;

	private static final int EXIT_CODE_REFERENCE_FILE_NOT_EXISTING = 2;

	private static final int EXIT_CODE_REFERENCE_FILE_NOT_READABLE = 3;

	private static final int EXIT_CODE_REFERENCE_FILE_IS_DIRECTORY = 4;

	private final static Level[] LOG_LEVELS = {Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR};
	
	public static void main(String[] args) {

		Options options = createCommandLineOptions();
		FilterPipelineCLIConfig config = parseCommandLineOptions(options, args);

		final HandlerFactoryRegistry factoryRegistry = new HandlerFactoryRegistry();
		ProtocolCollection.registerProtocols(factoryRegistry);

		final ExecutorService executorService = Executors.newCachedThreadPool();
		final ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(executorService));

		final FilterPipeline filterPipeline = new FilterPipelineImpl();
		try {
			filterPipeline.setChannelPipeline(factoryRegistry.create(config.getFilterPipelineConfigurationFile()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return filterPipeline;
			}
		}
		);

		// Create a new connection and wait until the connection is made successfully.
		ChannelFuture connectFuture = bootstrap.connect(new RXTXDeviceAddress(config.getPort()));
		final Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				channel.close().awaitUninterruptibly();
				bootstrap.releaseExternalResources();
			}
		}, "ShutdownThread"
		)
		);

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {

				ChannelBuffer buffer = ChannelBuffers.copiedBuffer(inputLine, Charset.defaultCharset());
				channel.write(buffer);

			}
		} catch (IOException e) {
			log.warn("{}", e);
		}

	}

	private static FilterPipelineCLIConfig parseCommandLineOptions(final Options options, final String[] args) {

		CommandLineParser parser = new PosixParser();

		String port = null;
		File filterPipelineConfigurationFile = null;

		try {

			CommandLine line = parser.parse(options, args, true);

			if (line.hasOption('h')) {
				printUsageAndExit(FilterPipelineCLI.class, options, 0);
			}

			if (line.hasOption('v')) {
				org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
			}

			if (line.hasOption('l')) {
				Level level = Level.toLevel(line.getOptionValue('l'));
				org.apache.log4j.Logger.getRootLogger().setLevel(level);
			}

			port = line.getOptionValue('p');
			filterPipelineConfigurationFile = new File(line.getOptionValue('f'));

			if (!filterPipelineConfigurationFile.exists()) {
				log.error("Filter pipeline configuration file {} does not exist!");
				System.exit(EXIT_CODE_REFERENCE_FILE_NOT_EXISTING);
			} else if (!filterPipelineConfigurationFile.canRead()) {
				log.error("Filter pipeline configuration file {} is not readable!");
				System.exit(EXIT_CODE_REFERENCE_FILE_NOT_READABLE);
			} else if (filterPipelineConfigurationFile.isDirectory()) {
				log.error("Filter pipeline configuration file {} is a directory!");
				System.exit(EXIT_CODE_REFERENCE_FILE_IS_DIRECTORY);
			}

		} catch (Exception e) {
			log.error("Invalid command line: " + e);
			printUsageAndExit(FilterPipelineCLI.class, options, EXIT_CODE_INVALID_ARGUMENTS);
		}

		return new FilterPipelineCLIConfig(port, filterPipelineConfigurationFile);
	}

	private static Options createCommandLineOptions() {

		Options options = new Options();

		Option portOption = new Option("p", "port", true, "Serial port to which the device is attached");
		portOption.setRequired(true);
		options.addOption(portOption);

		Option fileOption = new Option("f", "file", true, "A configuration file for the filter pipeline");
		fileOption.setRequired(true);
		options.addOption(fileOption);

		options.addOption("l", "logging", true,
				"Optional: set logging level (one of [" + Joiner.on(", ").join(LOG_LEVELS) + "])"
		);
		options.addOption("v", "verbose", false, "Verbose logging output");
		options.addOption("h", "help", false, "Help output");

		return options;
	}

	public static void printUsageAndExit(Class<?> clazz, Options options, int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(120, clazz.getCanonicalName(), null, options, null);
		System.exit(exitCode);
	}

}
