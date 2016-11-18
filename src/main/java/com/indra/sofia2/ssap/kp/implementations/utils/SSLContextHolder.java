/*******************************************************************************
 * Copyright 2013-16 Indra Sistemas S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 ******************************************************************************/
package com.indra.sofia2.ssap.kp.implementations.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.fusesource.hawtdispatch.transport.SslTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.exceptions.SSLContextInitializationError;

public class SSLContextHolder {

	private static final Logger log = LoggerFactory.getLogger(SSLContextHolder.class);
	private static SSLContext sslContext;

	public static SSLContext getSSLContext() throws SSLContextInitializationError {
		synchronized ("") {
			if (sslContext == null) {
				try {
					sslContext = initSSLContext();
				} catch (SSLContextInitializationError e) {
					throw e;
				} catch (Throwable e) {
					throw new SSLContextInitializationError(e);
				}
			}
			return sslContext;
		}
	}

	private static SSLContext initSSLContext() throws Exception {
		log.info("Initializing SSL context");
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		String keyStore = System.getProperty("javax.net.ssl.keyStore");
		String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

		if (algorithm == null || algorithm.trim().length() == 0) {
			throw new SSLContextInitializationError(
					"System property: ssl.KeyManagerFactory.algorithm cannot be null or empty");
		}
		if (keyStore == null || keyStore.trim().length() == 0) {
			throw new SSLContextInitializationError("System property: javax.net.ssl.keyStore cannot be null or empty");
		}
		if (keyStorePassword == null || keyStorePassword.trim().length() == 0) {
			throw new SSLContextInitializationError(
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
		log.info("The SSL context has been initialized successfully.");

		return sslContext;
	}
}