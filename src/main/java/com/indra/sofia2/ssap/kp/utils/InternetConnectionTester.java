package com.indra.sofia2.ssap.kp.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternetConnectionTester {

	private static final Logger log = LoggerFactory.getLogger(InternetConnectionTester.class);
	private static final String DEFAULT_TEST_URL = "http://www.google.es";

	private boolean isEnabled;
	private String testUrl;

	public InternetConnectionTester() {
		isEnabled = true;
		testUrl = DEFAULT_TEST_URL;
	}

	public InternetConnectionTester(boolean isEnabled) {
		this.isEnabled = isEnabled;
		testUrl = DEFAULT_TEST_URL;
	}

	public InternetConnectionTester(boolean isEnabled, String testUrl) {
		this.isEnabled = isEnabled;
		this.testUrl = testUrl;
	}

	public boolean testConnection() {
		if (!isEnabled)
			return true;
		HttpURLConnection conn = null;
		try {
			final URL url = new URL(testUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
				log.info("There's internet access.");
			} else {
				log.info("There's no internet access: unable to reach " + testUrl);
			}
		} catch (MalformedURLException e) {
			log.info("The test URL is malformed. The test has failed.");
			return false;
		} catch (IOException e) {
			log.info("There's no internet access: unable to reach " + testUrl);
			return false;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return true;
	}
}
