package de.uniluebeck.itm.tlspeerverficication;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class TlsPeerVerificationTrustManagerFactory extends TrustManagerFactorySpi {

    private final TlsPeerVerificationTrustManager trustManager;

    public TlsPeerVerificationTrustManagerFactory(TlsPeerVerificationTrustManager trustManager) {
        super();
        this.trustManager = trustManager;
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[] { trustManager };
    }

    @Override
    protected void engineInit(KeyStore arg0) throws KeyStoreException {
        // Nothing to do
    }

    @Override
    protected void engineInit(ManagerFactoryParameters arg0) throws InvalidAlgorithmParameterException {
        // Nothing to do
    }

}
