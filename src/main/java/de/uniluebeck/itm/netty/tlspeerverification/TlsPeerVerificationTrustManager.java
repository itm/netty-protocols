package de.uniluebeck.itm.netty.tlspeerverification;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.LoggerFactory;

public class TlsPeerVerificationTrustManager implements X509TrustManager {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TlsPeerVerificationTrustManager.class);
    
    private final TlsPeerVerificationKeyStore keyStore;

    public TlsPeerVerificationTrustManager(TlsPeerVerificationKeyStore keyStore) {
        super();
        this.keyStore = keyStore;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {  }; //TODO: keyStore.getRootCertificateAuthority()
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        log.debug("Checking client trust");
        //TODO
        //throw new CertificateException("Untrusted :-)");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        log.debug("Checking server trust");
        //TODO
        //throw new CertificateException("Untrusted :-)");
    }
}
