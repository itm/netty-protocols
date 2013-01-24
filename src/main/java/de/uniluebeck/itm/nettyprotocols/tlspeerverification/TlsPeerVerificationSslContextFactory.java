package de.uniluebeck.itm.nettyprotocols.tlspeerverification;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;

public class TlsPeerVerificationSslContextFactory {

	private static final String PROTOCOL = "TLS";

	public static SSLContext getContext(File rootCertificateAuthorityFile, File localCertificateFile,
										char[] certificatePassword, boolean clientMode)
			throws IOException, GeneralSecurityException {

		TlsPeerVerificationKeyStore keyStore =
				new TlsPeerVerificationKeyStore(rootCertificateAuthorityFile, localCertificateFile, certificatePassword
				);

		return getContext(keyStore, certificatePassword, clientMode);

	}

	public static SSLContext getContext(TlsPeerVerificationKeyStore keyStore, char[] certificatePassword,
										boolean clientMode)
			throws CertificateException, GeneralSecurityException, IOException {

		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");

		if (algorithm == null) {
			algorithm = "SunX509";
		}

		if (clientMode) {

			TlsPeerVerificationTrustManagerFactory trustManagerFactory =
					new TlsPeerVerificationTrustManagerFactory(null);

			SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
			sslContext.init(null, trustManagerFactory.engineGetTrustManagers(), null);

			return sslContext;

		} else {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(SecureChatKeyStore.asInputStream(), SecureChatKeyStore.getKeyStorePassword());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, SecureChatKeyStore.getCertificatePassword());

			// Set up key manager factory to use our key store

			SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
			sslContext.init(kmf.getKeyManagers(), null, null);
			// sslContext.init(kmf.getKeyManagers(), trustManagerFactory.engineGetTrustManagers(), null);
			return sslContext;
		}

	}
}
