package de.uniluebeck.itm.tlspeerverficication;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class TlsPeerVerificationTrustManager implements X509TrustManager {
    private final TlsPeerVerificationKeyStore keyStore;

    public TlsPeerVerificationTrustManager(TlsPeerVerificationKeyStore keyStore) {
        super();
        this.keyStore = keyStore;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] { keyStore.getRootCertificateAuthority() };
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        //TODO
        throw new CertificateException("Untrusted :-)");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        //TODO
        throw new CertificateException("Untrusted :-)");
    }
}
