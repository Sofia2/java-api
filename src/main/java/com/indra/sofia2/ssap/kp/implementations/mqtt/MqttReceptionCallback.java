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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSIBException;
import com.indra.sofia2.ssap.kp.exceptions.SSAPResponseTimeoutException;

/**
 * A class to receive the synchronous notifications for SSAP messages
 */
class MqttReceptionCallback {

	private static final Logger log = LoggerFactory.getLogger(MqttReceptionCallback.class);

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
			log.error("The callback of the internal MQTT client {} was interrupted while waiting for a response from the SIB server.",
					kpMqttClient.getMqttClientId());
		}
		if (response == null) {
			if (kpMqttClient.getSubscriptionThread().isStopped()) {
				log.error("The internal MQTT client {} has lost the connection with the SIB server.",
						kpMqttClient.getMqttClientId());
				kpMqttClient.getInternetConnectionTester().testConnection();
				throw new ConnectionToSIBException("The internal MQTT client has lost the connection with the SIB server");
			} else {
				log.error("The SSAP response timeout ({} milliseconds) has been exceeded. MqttClientId = {}.",
						kpMqttClient.getSsapResponseTimeout(), kpMqttClient.getMqttClientId());
				kpMqttClient.getInternetConnectionTester().testConnection();
				throw new SSAPResponseTimeoutException();
			}
		}
		return response;
	}

	void handle(String response) {
		log.debug("The callback of the internal MQTT client {} has received a response from the SIB server. Payload={}.",
				kpMqttClient.getMqttClientId(), response);
		this.response = response;
		latch.countDown();
	}
}