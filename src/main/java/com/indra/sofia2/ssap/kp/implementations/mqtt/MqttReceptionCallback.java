/*******************************************************************************
 * Copyright 2013-15 Indra Sistemas S.A.
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

package com.indra.sofia2.ssap.kp.implementations.mqtt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSIBException;
import com.indra.sofia2.ssap.kp.exceptions.SSAPResponseTimeoutException;

/**
 * A class to receive the synchronous notifications for SSAP messages
 */
class MqttReceptionCallback {

	private static final Log log = LogFactory.getLog(MqttReceptionCallback.class);

	private final CountDownLatch latch;
	private String response;
	private KpMQTTClient kpMqttClient;

	MqttReceptionCallback(KpMQTTClient kpMqttClient) {
		latch = new CountDownLatch(1);
		this.kpMqttClient = kpMqttClient;
	}

	String get() throws ConnectionToSIBException, SSAPResponseTimeoutException {
		try {
			latch.await(kpMqttClient.getSsapResponseTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			String errorMessage = String.format(
					"The callback of the internal MQTT client %s was interrupted while waiting for a response from the SIB server.",
					kpMqttClient.getMqttClientId());
			log.error(errorMessage);
			throw new RuntimeException(errorMessage, e);
		}
		if (response == null) {
			if (kpMqttClient.getSubscriptionThread().isStoped()) {
				String errorMessage = String.format(
						"The internal MQTT client %s has lost the connection with the SIB server.",
						kpMqttClient.getMqttClientId());
				log.error(errorMessage);
				kpMqttClient.getInternetConnectionTester().testConnection();
				throw new ConnectionToSIBException(errorMessage);
			} else {
				String errorMessage = String.format(
						"The internal MQTT client %s has exceeded the SSAP response timeout (%s ms)",
						kpMqttClient.getMqttClientId(), kpMqttClient.getSsapResponseTimeout());
				log.error(errorMessage);
				kpMqttClient.getInternetConnectionTester().testConnection();
				throw new SSAPResponseTimeoutException(errorMessage);
			}
		}
		return response;
	}

	void handle(String response) {
		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"The callback of the internal MQTT client %s has received a response from the SIB. Payload=%s",
					kpMqttClient.getMqttClientId(), response));
		}
		this.response = response;
		latch.countDown();
	}
}