package de.uniluebeck.itm.tlspeerverficication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.google.common.io.Files;

public class TlsPeerVerificationKeyStore {

    private final X509Certificate rootCertificateAuthority;

    private final X509Certificate localCertificate;

    private final char[] keyStorePassword;

    public TlsPeerVerificationKeyStore(File rootCertificateAuthorityFile, File localCertificateFile,
            char[] keyStorePassword) throws IOException, CertificateException {
        this(Files.toByteArray(rootCertificateAuthorityFile), Files.toByteArray(localCertificateFile), keyStorePassword);
    }

    public TlsPeerVerificationKeyStore(byte[] rootCertificateAuthority, byte[] localCertificate, char[] keyStorePassword)
            throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        this.rootCertificateAuthority =
                (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(
                        rootCertificateAuthority));
        this.localCertificate =
                (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(localCertificate));

        this.keyStorePassword = keyStorePassword;

    }

    public TlsPeerVerificationKeyStore(X509Certificate rootCertificateAuthority, X509Certificate localCertificate,
            char[] keyStorePassword) {
        this.rootCertificateAuthority = rootCertificateAuthority;
        this.localCertificate = localCertificate;
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * @return the rootCertificateAuthority
     */
    public X509Certificate getRootCertificateAuthority() {
        return rootCertificateAuthority;
    }

    /**
     * @return the localCertificate
     */
    public X509Certificate getLocalCertificate() {
        return localCertificate;
    }

    public KeyStore getAsKeyStore() throws KeyStoreException, GeneralSecurityException, CertificateException,
            IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, keyStorePassword);
        ks.setCertificateEntry("root-ca", getRootCertificateAuthority());
        ks.setCertificateEntry("local-cert", getLocalCertificate());
        return ks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TlsPeerVerificationKeyStore [rootCertificateAuthority=");
        builder.append(rootCertificateAuthority);
        builder.append(", localCertificate=");
        builder.append(localCertificate);
        builder.append("]");
        return builder.toString();
    }

    public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            FileNotFoundException, IOException {

        TlsPeerVerificationKeyStore keys =
                new TlsPeerVerificationKeyStore(new File("src/main/resources/testca/ca.pem"), new File(
                        "src/main/resources/testca/server.pem"), null);
        System.out.println(keys);

    }

}
