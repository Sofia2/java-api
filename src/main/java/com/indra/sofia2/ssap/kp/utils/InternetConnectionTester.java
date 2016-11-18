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
