package de.uniluebeck.itm.tlspeerverficication;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;

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
                    TlsPeerVerificationSslContextFactory.getContext(keyStore, certificatePassword).createSSLEngine();

            engine.setUseClientMode(clientMode);

            pipeline.addLast("ssl", new SslHandler(engine));

            // On top of the SSL handler, add the text line codec.
            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());

            // and then business logic.
            pipeline.addLast("handler", new LoggingHandler());

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

    /**
     * @param args
     * @throws Exception
     * @throws CertificateException
     */
    public static void main(String[] args) throws CertificateException, Exception {
        ChannelGroup allChannels = new DefaultChannelGroup();

        char[] certificatePassword = null;
        TlsPeerVerificationKeyStore keys =
                new TlsPeerVerificationKeyStore(new File("src/main/resources/testca/ca.pem"), new File(
                        "src/main/resources/testca/server.pem"), null);

        ClientBootstrap clientBootstrap =
                new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        clientBootstrap.setPipelineFactory(new PipelineFactory(true, certificatePassword, keys));

        ServerBootstrap serverBootstrap =
                new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        serverBootstrap.setPipelineFactory(new PipelineFactory(false, certificatePassword, keys));

        int serverPorts[] = new int[] { 9999 };

        for (int port : serverPorts)
            startServer(serverBootstrap, port);

        for (int port : serverPorts)
            allChannels.add(startClient(clientBootstrap, "localhost", port));

        // Shut down all thread pools to exit.
        allChannels.close().awaitUninterruptibly();
        clientBootstrap.releaseExternalResources();
        serverBootstrap.releaseExternalResources();
    }

}
