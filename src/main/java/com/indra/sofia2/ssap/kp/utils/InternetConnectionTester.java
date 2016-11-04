package com.indra.sofia2.ssap.kp.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.exceptions.NoInternetConnectionException;

public class InternetConnectionTester {

	private static final Logger log = LoggerFactory.getLogger(InternetConnectionTester.class);
	private static final String DEFAULT_TEST_URL = "http://www.google.com";

	private boolean isEnabled;
	private URL testUrl;

	public InternetConnectionTester() throws MalformedURLException {
		isEnabled = true;
		testUrl = new URL(DEFAULT_TEST_URL);
	}

	public InternetConnectionTester(boolean isEnabled) {
		this.isEnabled = isEnabled;
		try {
			testUrl = new URL(DEFAULT_TEST_URL);
		} catch (MalformedURLException e) {
			log.error("The default test URL is malformed. Exiting...");
			throw new RuntimeException(e);
		}
	}

	public InternetConnectionTester(boolean isEnabled, String testUrl) throws MalformedURLException {
		this.isEnabled = isEnabled;
		this.testUrl = new URL(testUrl);
	}

	public void testInternetConnectivity() throws NoInternetConnectionException {
		if (!isEnabled)
			return;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) testUrl.openConnection();
			conn.connect();
			if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
				log.info("There's internet access.");
			} else {
				log.info("There's no internet access: unable to reach " + testUrl);
			}
		} catch (IOException e) {
			log.error("There's no internet access: unable to reach " + testUrl);
			throw new NoInternetConnectionException(e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return;
	}
}
