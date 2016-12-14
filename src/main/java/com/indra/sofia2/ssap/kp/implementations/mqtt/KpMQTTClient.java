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
package com.indra.sofia2.ssap.kp.implementations.mqtt;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.KpToExtend;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSIBException;
import com.indra.sofia2.ssap.kp.exceptions.DnsResolutionException;
import com.indra.sofia2.ssap.kp.exceptions.SSLContextInitializationError;
import com.indra.sofia2.ssap.kp.implementations.mqtt.MqttConstants;
import com.indra.sofia2.ssap.kp.implementations.mqtt.exceptions.MQTTClientNotConfiguredException;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.kp.implementations.utils.SSLContextHolder;
import com.indra.sofia2.ssap.kp.utils.InternetConnectionTester;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.encryption.CypheredSSAPPayloadHandler;

public class KpMQTTClient extends KpToExtend {

	private static final Logger log = LoggerFactory.getLogger(KpMQTTClient.class);
	private static final int DEFAULT_DISCONNECTION_TIMEOUT = 5000;

	/**
	 * MQTT client to be used by the protocol to connect it to the MQTT server
	 * in SIB
	 */
	private MQTT mqttClient;

	/**
	 * Object that will be used to test the internet connection
	 */
	private InternetConnectionTester internetConnectionTester;

	/**
	 * MQTT connection between the MQTT client and the MQTT server in SIB
	 */
	private FutureConnection mqttConnection;

	/**
	 * Thread to receive SIB notifications, regardless if it is SSAP or not
	 */
	private MqttSubscriptionThread subscriptionThread;

	/**
	 * Queue to store ssap message responses
	 */
	private MqttReceptionCallback responseCallback = null;

	/**
	 * The MQTT clientID to be used.
	 */
	private String mqttClientId;
	
	/**
	 * This object will encrypt/decrypt the SSAP payloads (if necessary)
	 */
	private CypheredSSAPPayloadHandler cypheredPayloadHandler;

	/**
	 * 
	 * @param config
	 * @throws ConnectionConfigException
	 */
	public KpMQTTClient(MQTTConnectionConfig config, String kp, String kpInstance, String token) throws ConnectionConfigException {
		super(config, kp, kpInstance, token);
		this.internetConnectionTester = new InternetConnectionTester(config.isCheckInternetConnection());
	}

	/**
	 * Creates a MQTT client and connects it to the SIB server
	 */
	@Override
	public synchronized void connect() throws ConnectionToSIBException {

		try {
			log.info("Establishing MQTT connection with SIB server {} using port {}.", config.getSibHost(),
					config.getSibPort());

			MQTTConnectionConfig cfg = (MQTTConnectionConfig) config;

			String sibHost = getSibHost(cfg);
			initializeIndicationPool();

			ssapResponseTimeout = cfg.getSsapResponseTimeout();
			boolean cleanSession = cfg.isCleanSession();
			
			if (xxteaCipherKey != null) {
				log.info("Configuring cypered SSAP payload handler...");
				cypheredPayloadHandler = new CypheredSSAPPayloadHandler(xxteaCipherKey);
			} else {
				log.warn("No xxtea cypher key has been specified. XXTEA-cyphered SSAP messages won't be supported.");
			}

			// Creates the client (if needed) and open a connection to the SIB
			// server
			configureMqttClient(cfg, sibHost);

			if (mqttConnection == null || !mqttConnection.isConnected()) {
				mqttClient.setCleanSession(cleanSession);
				mqttConnection = mqttClient.futureConnection();
				int connectTimeout = config.getSibConnectionTimeout();
				log.info("Establishing MQTT connection with the SIB server. MqttClientId = {}.", mqttClientId);
				if (connectTimeout == 0) {
					log.warn(
							"The SIB connect timeout is zero. We will wait for a connection indefinitely. MqttClientId = {}.",
							mqttClientId);
					mqttConnection.connect().await();
				} else {
					mqttConnection.connect().await(connectTimeout, TimeUnit.MILLISECONDS);
				}
			}

			// Subscribes to the KP in order to receive any kind of SIB
			// notifications
			subscribeToSibMqttTopics();

			// Notifica que se ha realizado la conexión a los listener
			if (isPhysicalConnectionEstablished()) {
				notifyConnectionEvent();
			}
			log.info("The internal MQTT client {} has established a connection to the SIB server.", mqttClientId);

		} catch (Throwable e) {
			log.error("Unable to connect internal MQTT client to the SIB server. Cause = {}, errorMessage = {}.",
					e.getCause(), e.getMessage());
			this.disconnect();
			if (!(e instanceof URISyntaxException)) {
				internetConnectionTester.testInternetConnectivity();
			}
			throw new ConnectionToSIBException(e);
		}
	}

	@Override
	public boolean isPhysicalConnectionEstablished() {
		return mqttConnection != null && mqttConnection.isConnected();
	}

	/**
	 * Disconnect the MQTT client from the SIB server
	 */
	@Override
	public synchronized void disconnect() {
		if (mqttConnection != null) {
			try {
				// Stop the thread that handles notifications from the SIB server
				if (this.subscriptionThread != null && !this.subscriptionThread.isStopped()) {
					log.info("Stopping SIB subscription thread of the internal MQTT client {}.", mqttClientId);
					this.subscriptionThread.myStop();
				}
			} catch (Exception e) {
				log.error(
						"Unable to stop SIB subscription thread of the internal MQTT client {}. Cause = {}, errorMessage = {}.",
						mqttClientId, e.getCause(), e.getMessage());
			}

			if (mqttConnection.isConnected()) {
				unsubscribeFromSibMqttTopics();
			}

			try {
				log.info("Disconnecting internal MQTT client {} from the SIB server.", mqttClientId);
				int timeout = config.getSibConnectionTimeout();
				if (timeout == 0) {
					log.warn(
							"The SIB connect timeout is zero. We will wait for {} milliseconds before interrupting the connection. MqttClientId = {}.",
							DEFAULT_DISCONNECTION_TIMEOUT, mqttClientId);
					mqttConnection.kill().await(DEFAULT_DISCONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
				} else {
					try {
						log.info("Disconnecting internal MQTT client {} from the SIB server.", mqttClientId);
						mqttConnection.kill().await(timeout, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						log.warn(
								"A timeout error was detected while disconnecting the internal MQTT client {}. Trying to close the connection again.",
								mqttClientId);
						mqttConnection.disconnect().await(timeout, TimeUnit.MILLISECONDS);
					}
				}

				log.info("The internal MQTT client {} has been disconnected from the SIB server successfully.",
						mqttClientId);
			} catch (Exception e) {
				log.warn("The internal MQTT client {} had a disconnection error. Cause = {}, errorMessage = {}.",
						mqttClientId, e.getCause(), e.getMessage());
			} finally {
				mqttConnection = null;
				this.destroyIndicationPool();
				this.notifyDisconnectionEvent();
			}

		} else {
			log.info("The internal MQTT client {} is already disconnected from the SIB server. Nothing will be done.",
					mqttClientId);
		}
	}

	@Override
	protected void notifyConnectionEvent() {
		log.info(
				"Notifying connection event of the internal MQTT client {} to the connection events listener (if exists).",
				mqttClientId);
		super.notifyConnectionEvent();
	}

	@Override
	protected void notifyDisconnectionEvent() {
		log.info(
				"Notifying disconnection event of the internal MQTT client {} to the connection events listener (if exists).",
				mqttClientId);
		super.notifyDisconnectionEvent();
	}

	@Override
	protected void destroyIndicationPool() {
		log.info("Destroying indication pool of the internal MQTT client {}.", mqttClientId);
		super.destroyIndicationPool();
	}

	/**
	 * Subscribe the MQTT client to the topics that the SIB server will use to
	 * notify SSAP responses
	 */
	private void subscribeToSibMqttTopics() throws ConnectionToSIBException {

		subscribeToMqttTopic(MqttConstants.getSsapResponseMqttTopic(mqttClientId));
		subscribeToMqttTopic(MqttConstants.getSsapIndicationMqttTopic(mqttClientId));

		// Launch a Thread to receive notifications
		if (this.subscriptionThread == null || this.subscriptionThread.isStopped()) {
			log.info("Starting subscription thread of the internal MQTT client {}.", mqttClientId);
			this.subscriptionThread = new MqttSubscriptionThread(this);
			this.subscriptionThread.start();
		}

	}

	/**
	 * Unsubscribe the MQTT client to the topics that the SIB server will use to
	 * notify SSAP responses
	 */
	private void unsubscribeFromSibMqttTopics() {
		unsubscribeFromMqttTopic(MqttConstants.getSsapResponseMqttTopic(mqttClientId));
		unsubscribeFromMqttTopic(MqttConstants.getSsapIndicationMqttTopic(mqttClientId));
	}
	
	private SSAPMessage sendSsapMessageToSib(SSAPMessage msg, boolean encryptPayload) throws ConnectionToSIBException {
		log.debug("Sending SSAP message to the SIB server using the internal MQTT client {}. Payload={}.", mqttClientId,
				msg.toJson());
		internetConnectionTester.testInternetConnectivity();
		try {
			SSAPMessage ssapResponse;
			synchronized (this) {
				this.responseCallback = new MqttReceptionCallback(this);

				// Publish a QoS message
				// It is not necessary publish topic. The message will be
				// received by the handler in server
				QoS qosLevel = ((MQTTConnectionConfig) config).getQualityOfService();
				log.debug(
						"Sending MQTT PUBLISH message to the SIB server using the internal MQTT client {}. QoS={}, encryptPayload = {}, payload = {}.",
						mqttClientId, qosLevel, encryptPayload, msg.toJson());
				byte[] payload;
				if (encryptPayload)
					payload = cypheredPayloadHandler.getEncryptedPayload(msg);
				else
					payload = msg.toJson().getBytes();
				mqttConnection.publish(MqttConstants.SIB_REQUESTS_TOPIC, payload, qosLevel, false);
				
				String responsePayload = this.responseCallback.get();
				if (encryptPayload) {
					responsePayload = cypheredPayloadHandler.getDecryptedPayload(responsePayload);
				}
				ssapResponse = SSAPMessage.fromJsonToSSAPMessage(responsePayload);
				log.debug(
						"The internal MQTT client {} received a SSAP response from the SIB server. Response={}, request={}.",
						mqttClientId, ssapResponse.toJson(), msg.toJson());
				responseCallback = null;
			}
			return ssapResponse;

		} catch (Throwable e) {
			log.error(
					"Unable to send SSAP message to the SIB server using internal MQTT client {}. Payload = {}, cause = {}, errorMessage = {}.",
					mqttClientId, msg.toJson(), e.getCause(), e.getMessage());
			responseCallback = null;
			internetConnectionTester.testInternetConnectivity();
			throw new ConnectionToSIBException("Unable to send SSAP message to the SIB server", e);
		}
	}
	
	/**
	 * Send a SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage send(SSAPMessage msg) throws ConnectionToSIBException {
		return sendSsapMessageToSib(msg, false);
	}

	/**
	 * Send an encrypted SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage sendCipher(SSAPMessage msg) throws ConnectionToSIBException {
		if (xxteaCipherKey == null) {
			log.error("No XXTEA cypher key has been specified. The SSAP message can only be sent in plain mode.");
			throw new IllegalStateException("No XXTEA cypher key has been specified");
		}
		return sendSsapMessageToSib(msg, true);
	}

	/**
	 * Returns the MQTT clientID for this KP
	 * 
	 * @return
	 * @throws MQTTClientNotConfiguredException
	 */
	public String getClientId() throws MQTTClientNotConfiguredException {
		if (this.mqttClient == null) {
			throw new MQTTClientNotConfiguredException(
					"You must configure the internal MQTT client in order to get its MQTT clientID.");
		} else {
			return mqttClientId;
		}
	}

	/*
	 * ********************************************************************
	 * Auxiliary functions
	 * ********************************************************************
	 */

	private void configureMqttClient(MQTTConnectionConfig cfg, String sibHost)
			throws SSLContextInitializationError, ConnectionToSIBException, URISyntaxException {
		if (mqttClient == null) {
			log.info("Configuring internal MQTT client. MqttClientId = {}.", cfg.getClientId());
			mqttClient = new MQTT();
			String username = cfg.getUser();
			String password = cfg.getPassword();
			if (username != null) {
				mqttClient.setUserName(username);
			}
			if (password != null) {
				mqttClient.setPassword(password);
			}

			mqttClientId = cfg.getClientId();
			mqttClient.setClientId(cfg.getClientId());

			if (sibHost.startsWith("ssl://")) {
				mqttClient.setHost( sibHost + ":" + config.getSibPort());
				mqttClient.setSslContext(SSLContextHolder.getSSLContext());
			} else {
				mqttClient.setHost(sibHost, config.getSibPort());
			}				

			// Configure low-level parameters of the fuse MQTT client
			mqttClient.setReconnectAttemptsMax(cfg.getReconnectAttemptsMax());
			mqttClient.setConnectAttemptsMax(cfg.getConnectAttemptsMax());
			mqttClient.setReconnectDelay(cfg.getReconnectDelay());
			mqttClient.setReconnectDelayMax(cfg.getReconnectDelayMax());
			mqttClient.setReconnectBackOffMultiplier(cfg.getReconnectBackOffMultiplier());
			mqttClient.setReceiveBufferSize(cfg.getReceiveBufferSize());
			mqttClient.setSendBufferSize(cfg.getSendBufferSize());
			mqttClient.setTrafficClass(cfg.getTrafficClass());
			mqttClient.setMaxReadRate(cfg.getMaxReadRate());
			mqttClient.setMaxWriteRate(cfg.getMaxWriteRate());
			mqttClient.setKeepAlive((short) cfg.getKeepAliveInSeconds());
			log.info("The internal MQTT client has been configured. MqttClientId = {}.", cfg.getClientId());
		}
	}

	private String getSibHost(MQTTConnectionConfig cfg) throws ConnectionToSIBException {
		String sibHost;
		try {
			if (config.getSibHost().startsWith("tcp://") || config.getSibHost().startsWith("ssl://")) {
				InetAddress.getByName(config.getSibHost().substring(6));
			} else {
				InetAddress.getByName(config.getSibHost());
			}
			sibHost = config.getSibHost();
			log.info("The hostname of the SIB server ({}) can be resolved. The connection process will continue.",
					config.getSibHost());
		} catch (java.net.UnknownHostException e) {
			if (cfg.getDnsFailHostSIB() == null || cfg.getDnsFailHostSIB().trim().length() == 0) {
				log.error("Unable to resolve hostname of the SIB server ({}). Checking internet connectivity.",
						config.getSibHost());
				internetConnectionTester.testInternetConnectivity();
				throw new DnsResolutionException("Unable to resolve hostname of the SIB server");
			} else {
				sibHost = cfg.getDnsFailHostSIB();
				log.warn("Unable to resolve hostname of the SIB server ({}). Using fallback IP address ({}).",
						config.getSibHost(), cfg.getDnsFailHostSIB());
			}
		}
		return sibHost;
	}

	private void subscribeToMqttTopic(String topicName) throws ConnectionToSIBException {
		Future<byte[]> subscribeFuture = null;

		// Subscription to topic for ssap response messages
		QoS qosLevel = ((MQTTConnectionConfig) config).getQualityOfService();
		try {
			log.info("Subscribing internal MQTT client {} to SIB topic {} with QoS={}.", mqttClientId, topicName,
					qosLevel);
			Topic[] topics = { new Topic(topicName, qosLevel) };
			subscribeFuture = mqttConnection.subscribe(topics);

			int timeout = config.getSibConnectionTimeout();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			log.error(
					"Unable to subscribe internal MQTT client {} to the SIB MQTT topic {} with QoS={}. The connection process will be aborted. Cause = {}, errorMessage = {}.",
					mqttClientId, topicName, qosLevel, e.getCause(), e.getMessage());
			throw new ConnectionToSIBException("Unable to subscribe internal MQTT client to topic " + topicName, e);
		}
	}

	private void unsubscribeFromMqttTopic(String publicationTopicName) {
		Future<Void> subscribeFuture = null;
		// Unsubscription to topic for ssap response messages
		try {
			log.info("Unsubscribing internal MQTT client {} from the SIB MQTT topic {}.", mqttClientId,
					publicationTopicName);
			String[] topics = { new String(publicationTopicName) };

			subscribeFuture = mqttConnection.unsubscribe(topics);
			int timeout = config.getSibConnectionTimeout();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}

		} catch (Exception e) {
			log.warn(
					"Unable to unsubscribe internal MQTT client {} from the SIB MQTT topic {}. Cause = {}, errorMessage = {}.",
					mqttClientId, publicationTopicName, e.getCause(), e.getMessage());
		}
	}

	/*
	 * ********************************************************************
	 * Getters to be used by the MQTT callbacks and worker threads
	 * ********************************************************************
	 */

	String getMqttClientId() {
		return this.mqttClientId;
	}

	List<Listener4SIBIndicationNotifications> getSubscriptionListeners() {
		return this.subscriptionListeners;
	}

	String getBaseCommandRequestSubscriptionId() {
		return this.baseCommandRequestSubscriptionId;
	}

	Listener4SIBIndicationNotifications getListener4BaseCommandRequestNotifications() {
		return this.listener4BaseCommandRequestNotifications;
	}

	String getStatusControlRequestSubscriptionId() {
		return this.statusControlRequestSubscriptionId;
	}

	Listener4SIBIndicationNotifications getListener4StatusControlRequestNotifications() {
		return listener4StatusControlRequestNotifications;
	}

	String getXxteaCipherKey() {
		return this.xxteaCipherKey;
	}

	FutureConnection getMqttConnection() {
		return mqttConnection;
	}

	MqttReceptionCallback getResponseCallback() {
		return responseCallback;
	}

	MqttSubscriptionThread getSubscriptionThread() {
		return subscriptionThread;
	}

	InternetConnectionTester getInternetConnectionTester() {
		return internetConnectionTester;
	}
	
	CypheredSSAPPayloadHandler getCypheredPayloadHandler() {
		return cypheredPayloadHandler;
	}

	/*
	 * ********************************************************************
	 * Auxiliary methods used by the MQTT callbacks and worker threads
	 * ********************************************************************
	 */

	void runIndicationTasks(Collection<IndicationTask> indicationTasks) {
		super.executeIndicationTasks(indicationTasks);
	}
}