package de.uniluebeck.itm.nettyprotocols.tlspeerverification;

import org.slf4j.LoggerFactory;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class TlsPeerVerificationTrustManagerFactory extends TrustManagerFactorySpi {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(TlsPeerVerificationTrustManagerFactory.class);

	private final TlsPeerVerificationTrustManager trustManager;

	public TlsPeerVerificationTrustManagerFactory(TlsPeerVerificationTrustManager trustManager) {
		super();
		this.trustManager = trustManager;
	}

	@Override
	protected TrustManager[] engineGetTrustManagers() {
		log.debug("Returning custom trust manager");
		return new TrustManager[]{trustManager};
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
