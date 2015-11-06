package com.indra.sofia2.ssap.kp.implementations.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.hawtdispatch.transport.SslTransport;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;

public class SSLContextHolder {

	private static final Log log = LogFactory.getLog(SSLContextHolder.class);
	private static SSLContext sslContext;

	public static SSLContext getSSLContext() throws UnrecoverableKeyException, KeyManagementException,
			FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		synchronized ("") {
			if (sslContext == null) {
				sslContext = initSSLContext();
			}
			return sslContext;
		}
	}

	private static SSLContext initSSLContext() throws FileNotFoundException, KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		log.info("Initializing SSL context\n");
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		String keyStore = System.getProperty("javax.net.ssl.keyStore");
		String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

		if (algorithm == null || algorithm.trim().length() == 0) {
			throw new ConnectionToSibException(
					"System property: ssl.KeyManagerFactory.algorithm cannot be null or empty");
		}
		if (keyStore == null || keyStore.trim().length() == 0) {
			throw new ConnectionToSibException("System property: javax.net.ssl.keyStore cannot be null or empty");
		}
		if (keyStorePassword == null || keyStorePassword.trim().length() == 0) {
			throw new ConnectionToSibException(
					"System property: javax.net.ssl.keyStorePassword cannot be null or empty");
		}

		FileInputStream fin = new FileInputStream(keyStore);
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(fin, keyStorePassword.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
		kmf.init(ks, keyStorePassword.toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
		tmf.init(ks);

		SSLContext sslContext = SSLContext.getInstance(SslTransport.protocol("ssl"));
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		log.info("The SSL context has been initialized successfully.\n");

		return sslContext;
	}
}