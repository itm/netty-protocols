package de.uniluebeck.itm.nettyprotocols.tlspeerverification;

import de.uniluebeck.itm.nettyprotocols.logging.LoggingHandler;
import org.apache.log4j.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;

public class Main {

	static class PipelineFactory implements ChannelPipelineFactory {

		private final boolean clientMode;

		public char[] certificatePassword;

		public TlsPeerVerificationKeyStore keyStore;

		public PipelineFactory(boolean clientMode, char[] certificatePassword, TlsPeerVerificationKeyStore keyStore) {
			super();
			this.clientMode = clientMode;
			this.certificatePassword = certificatePassword;
			this.keyStore = keyStore;
		}

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();

			// Add SSL handler first to encrypt and decrypt everything.
			SSLEngine engine =
					TlsPeerVerificationSslContextFactory.getContext(keyStore, certificatePassword, clientMode)
							.createSSLEngine();

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{
					new X509TrustManager() {
						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}

						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							// Always trust - it is an example.
							// You should do something in the real world.
							// You will reach here only if you enabled client certificate auth,
							// as described in SecureChatSslContextFactory.
							System.err.println("UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
						}

						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							// Always trust - it is an example.
							// You should do something in the real world.
							System.err.println("UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
						}
					}
			}, null
			);

			engine.setUseClientMode(clientMode);

			pipeline.addLast("ssl", new SslHandler(engine));
			pipeline.addLast("tls-peer-verification", new TlsPeerVerificationHandler());

			// On top of the SSL handler, add the text line codec.
			pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
			pipeline.addLast("decoder", new StringDecoder());
			pipeline.addLast("encoder", new StringEncoder());

			pipeline.addLast("handler", new LoggingHandler("logger"));

			return pipeline;
		}

	}

	public static void startServer(ServerBootstrap serverBootstrap, int port) {
		serverBootstrap.bind(new InetSocketAddress(port));
	}

	public static Channel startClient(ClientBootstrap clientBootstrap, String host, int port) {
		ChannelFuture future = clientBootstrap.connect(new InetSocketAddress(host, port));
		return future.awaitUninterruptibly().getChannel();
	}

	private static void configureLoggingDefaults() {
		PatternLayout patternLayout = new PatternLayout("%-13d{HH:mm:ss,SSS} | %-25.25c{2} | %-5p | %m%n");

		final Appender appender = new ConsoleAppender(patternLayout);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	/**
	 * @param args
	 *
	 * @throws Exception
	 * @throws CertificateException
	 */
	public static void main(String[] args) throws CertificateException, Exception {
		configureLoggingDefaults();
		ChannelGroup allChannels = new DefaultChannelGroup();

		ClientBootstrap clientBootstrap =
				new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()
				)
				);

		clientBootstrap.setPipelineFactory(new PipelineFactory(true, "secret".toCharArray(), null));

		ServerBootstrap serverBootstrap =
				new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()
				)
				);

		serverBootstrap.setPipelineFactory(new PipelineFactory(false, "secret".toCharArray(), null));

		int serverPorts[] = new int[]{9999};

		// if ("server".equals(args[0]))
		for (int port : serverPorts) {
			startServer(serverBootstrap, port);
		}

		// if ("client".equals(args[0]))
		for (int port : serverPorts) {
			allChannels.add(startClient(clientBootstrap, "localhost", port));
		}

		// Read commands from the stdin.
		for (BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			 !"exit".equals(in.readLine()); ) {
			System.out.println("Please enter exit to quit.");
		}

		// Shut down all thread pools to exit.
		System.out.println("Shuting down.");
		allChannels.close().awaitUninterruptibly();
		serverBootstrap.releaseExternalResources();
		clientBootstrap.releaseExternalResources();
		System.exit(1);
	}
}
