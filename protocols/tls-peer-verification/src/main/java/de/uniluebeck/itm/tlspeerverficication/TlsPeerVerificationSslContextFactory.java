package de.uniluebeck.itm.tlspeerverficication;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class TlsPeerVerificationSslContextFactory {
    private static final String PROTOCOL = "TLS";

    public static SSLContext getContext(File rootCertificateAuthorityFile, File localCertificateFile,
            char[] certificatePassword) throws IOException, GeneralSecurityException {

        TlsPeerVerificationKeyStore keyStore =
                new TlsPeerVerificationKeyStore(rootCertificateAuthorityFile, localCertificateFile, certificatePassword);

        return getContext(keyStore, certificatePassword);

    }

    public static SSLContext getContext(TlsPeerVerificationKeyStore keyStore, char[] certificatePassword)
            throws CertificateException, GeneralSecurityException, IOException {

        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");

        if (algorithm == null) {
            algorithm = "SunX509";
        }

        // Set up key manager factory to use our key store
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(keyStore.getAsKeyStore(), certificatePassword);

        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(kmf.getKeyManagers(), null, null);

        return sslContext;

    }
}
