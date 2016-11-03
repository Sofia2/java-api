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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.encryption.XXTEA;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;
import com.indra.sofia2.ssap.kp.implementations.KpToExtend;
import com.indra.sofia2.ssap.kp.implementations.mqtt.exceptions.MQTTClientNotConfiguredException;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.kp.implementations.utils.SSLContextHolder;
import com.indra.sofia2.ssap.kp.utils.InternetConnectionTester;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinUserAndPasswordMessage;

public class KpMQTTClient extends KpToExtend {
	
	private static Logger log = Logger.getLogger(KpMQTTClient.class.getName());

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
	 * Lock to block a request until receive the synchronous response from SIB
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * Queue to store ssap message responses
	 */
	private MqttReceptionCallback responseCallback = null;

	/**
	 * The MQTT clientID to be used.
	 */
	private String mqttClientId;

	/**
	 * Legacy constructor. The internet connectivity tests are disabled.
	 * 
	 * @param config
	 * @throws ConnectionConfigException
	 */
	public KpMQTTClient(MQTTConnectionConfig config) throws ConnectionConfigException {
		super(config);
		this.internetConnectionTester = new InternetConnectionTester(false);
	}
	
	/**
	 * New constructor. The internet connectivity tests can be enabled or disabled according
	 * to its arguments.
	 * @param config
	 * @param enableInternetConnectionTests
	 * @throws ConnectionConfigException
	 */
	public KpMQTTClient(MQTTConnectionConfig config, boolean enableInternetConnectionTests) throws ConnectionConfigException{
		super(config);
		this.internetConnectionTester = new InternetConnectionTester(enableInternetConnectionTests);
	}

	/**
	 * Creates a MQTT client and connects it to the MQTT server in SIB
	 */
	@Override
	public void connect() throws ConnectionToSibException {

		String sibAddress = null;
		try {
			log.info(String.format("Establishing MQTT connection with SIB server %s using port %s.",
					config.getHostSIB(), config.getPortSIB()));

			MQTTConnectionConfig cfg = (MQTTConnectionConfig) config;

			sibAddress = getSibAddress(cfg);
			this.initializeIndicationPool();

			ssapResponseTimeout = cfg.getSsapResponseTimeout();
			boolean cleanSession = cfg.isCleanSession();

			// Creates the client (if needed) and open a connection to the SIB
			configureMqttClient(cfg, sibAddress);

			if (mqttConnection == null || !mqttConnection.isConnected()) {
				mqttClient.setCleanSession(cleanSession);
				mqttConnection = mqttClient.futureConnection();

				int timeout = config.getTimeOutConnectionSIB();
				if (timeout == 0) {
					mqttConnection.connect().await();
				} else {
					mqttConnection.connect().await(timeout, TimeUnit.MILLISECONDS);
				}
			}

			// Subscribes to the KP in order to receive any kind of SIB
			// notifications
			this.subscribeToSibMqttTopics();

			// Notifica que se ha realizado la conexión a los listener
			if (this.isConnectionEstablished()) {
				this.notifyConnectionEvent();
			}
			log.info(String.format("The internal MQTT client %s has established a connection to the SIB server.",
					mqttClientId));

		} catch (URISyntaxException e) {
			String errorMessage = String.format("The internal MQTT client %s couldn't connect to the SIB server %s:%s",
					sibAddress, config.getPortSIB());
			log.error(errorMessage, e);
			this.disconnect();
			throw new ConnectionToSibException(errorMessage, e);
		} catch (Exception e) {
			String errorMessage = String.format("Unable to connnect the internal MQTT client to the SIB server %s:%s.",
					sibAddress, config.getPortSIB());
			log.error(errorMessage, e);
			this.disconnect();
			internetConnectionTester.testConnection();
			throw new ConnectionToSibException(errorMessage, e);
		}
	}

	@Override
	public boolean isConnected() {
		return isJoined() && isConnectionEstablished();
	}

	@Override
	public boolean isConnectionEstablished() {
		return mqttConnection != null && mqttConnection.isConnected();
	}

	public boolean isJoined() {
		return joined;
	}

	/**
	 * Disconnect the MQTT client from the SIB
	 */
	@Override
	public synchronized void disconnect() {
		log.info(String.format("Disconnecting internal MQTT client %s from the SIB server.", mqttClientId));

		if (mqttConnection != null) {
			// Cierra el hilo de recepción
			try {
				// Stop the thread that waits for notifications from SIB
				if (this.subscriptionThread != null && !this.subscriptionThread.isStoped()) {
					log.info(String.format("Stopping SIB subscription thread of the internal MQTT client %s.",
							mqttClientId));
					this.subscriptionThread.myStop();
				}
			} catch (Exception e) {
				log.error(String.format("Unable to stop SIB subscription thread of the internal MQTT client %s.",
						mqttClientId), e);
			}

			// Se desuscribe de topicos de notificacion
			if (mqttConnection.isConnected()) {
				unsubscribeFromSibMqttTopics();
			}

			// Cierra la conexión física
			try {

				int timeout = config.getTimeOutConnectionSIB();
				if (timeout == 0) {
					mqttConnection.kill().await(DEFAULT_DISCONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
				} else {
					try {
						log.info(String.format("Disconnecting internal MQTT client %s from the SIB server.",
								mqttClientId));
						mqttConnection.kill().await(timeout, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						log.warn(String.format(
								"A timeout error was detected while disconnecting the internal MQTT client %s. Trying to close the connection again...",
								mqttClientId));
						mqttConnection.disconnect().await(timeout, TimeUnit.MILLISECONDS);
					}
				}

				log.info(String.format("The internal MQTT client %s has disconnected from the SIB server successfully.",
						mqttClientId));
			} catch (Exception e) {
				log.error(String.format("Ignoring disconnect error of the internal MQTT client %s.", mqttClientId), e);
			} finally {
				joined = false;
				mqttConnection = null;
				this.destroyIndicationPool();
				this.notifyDisconnectionEvent();
			}

		} else {
			log.info(String.format(
					"The internal MQTT client %s was disconnected from the SIB server. Nothing will be done.",
					mqttClientId));
			joined = false;
			this.destroyIndicationPool();
			this.notifyDisconnectionEvent();
		}
	}

	@Override
	protected void notifyConnectionEvent() {
		log.info(String.format(
				"Notifying connection event of the internal MQTT client %s to the connection events listener (if exists).",
				mqttClientId));
		super.notifyConnectionEvent();
	}

	@Override
	protected void notifyDisconnectionEvent() {
		log.info(String.format(
				"Notifying disconnection event of the internal MQTT client %s to the connection events listener (if exists).",
				mqttClientId));
		super.notifyDisconnectionEvent();
	}

	@Override
	protected void destroyIndicationPool() {
		log.info(String.format("Destroying indication pool of the internal MQTT client %s.", mqttClientId));
		super.destroyIndicationPool();
	}

	/**
	 * Subscribe the MQTT client to the topics that the SIB will use to notify
	 * messages from SIB
	 */
	private void subscribeToSibMqttTopics() {
		
		subscribeToMqttTopic(MqttConstants.getSsapResponseMqttTopic(mqttClientId));
		subscribeToMqttTopic(MqttConstants.getSsapIndicationMqttTopic(mqttClientId));

		// Launch a Thread to receive notifications
		if (this.subscriptionThread == null || this.subscriptionThread.isStoped()) {
			log.info(String.format("Starting subscription thread of the internal MQTT client %s.", mqttClientId));
			this.subscriptionThread = new MqttSubscriptionThread(this);
			this.subscriptionThread.start();
		}

	}

	/**
	 * Unsubscribe the MQTT client to the topics that the SIB will use to notify
	 * messages from SIB
	 */
	private void unsubscribeFromSibMqttTopics() {

		unsubscribeFromMqttTopic(MqttConstants.getSsapResponseMqttTopic(mqttClientId));
		unsubscribeFromMqttTopic(MqttConstants.getSsapIndicationMqttTopic(mqttClientId));

	}

	/**
	 * Send a SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage send(SSAPMessage msg) throws ConnectionToSibException {
		if (log.isDebugEnabled()){
			log.debug(String.format("Sending SSAP message to the SIB server using the internal MQTT client %s. Payload=%s.",
					mqttClientId, msg.toJson()));
		}
		internetConnectionTester.testConnection();
		// Sends the message to Server
		try {
			MqttReceptionCallback callback = new MqttReceptionCallback(this);
			SSAPMessage ssapResponse;
			synchronized (this) {
				this.responseCallback = callback;

				// Publish a QoS message
				// It is not necessary publish topic. The message will be
				// received by the handler in server
				QoS qosLevel = ((MQTTConnectionConfig) config).getQualityOfService();
				if (log.isDebugEnabled()){
					log.debug(String.format(
							"Sending MQTT PUBLISH message to the SIB server using the internal MQTT client %s. QoS=%s, Payload=%s.",
							mqttClientId, qosLevel, msg.toJson()));
				}
				mqttConnection.publish("", msg.toJson().getBytes(),
						qosLevel, false);
				ssapResponse = SSAPMessage.fromJsonToSSAPMessage(callback.get());
				if (log.isDebugEnabled()){
					log.debug(String.format(
							"The internal MQTT client %s received a SSAP response from the SIB server. Payload=%s, Original request=%s.",
							mqttClientId, ssapResponse.toJson(), msg.toJson()));
				}
				responseCallback = null;
			}
			return ssapResponse;

		} catch (Throwable e) {
			String errorMessage = String.format("Unable to send SSAP message to the SIB server using internal MQTT client %s. Payload=%s.", 
					mqttClientId, msg.toJson());
			log.error(errorMessage, e);
			responseCallback = null;
			internetConnectionTester.testConnection();
			throw new ConnectionToSibException(errorMessage, e);
		}
	}

	/**
	 * Send a SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage sendCipher(SSAPMessage msg) throws ConnectionToSibException {
		if (log.isDebugEnabled()){
			log.debug(String.format("Sending cyphered SSAP message to the SIB server using the internal MQTT client %s. Payload=%s.",
					mqttClientId, msg.toJson()));
		}
		// Sends the message to Server
		try {

			MqttReceptionCallback callback = new MqttReceptionCallback(this);
			lock.lock();// Blocks until receive the ssap response
			try {
				// callbacks.add(callback);
				this.responseCallback = callback;
				byte[] encrypted = XXTEA.encrypt(msg.toJson().getBytes(), this.xxteaCipherKey.getBytes());
				byte[] base64Payload;
				if (msg.getMessageType() == SSAPMessageTypes.JOIN) {
					SSAPBodyJoinUserAndPasswordMessage body = SSAPBodyJoinUserAndPasswordMessage
							.fromJsonToSSAPBodyJoinUserAndPasswordMessage(msg.getBody());

					String kpName = body.getInstance().split(":")[0];

					String completeMessage = kpName.length() + "#" + kpName + Base64.encodeBase64String(encrypted);

					base64Payload = completeMessage.getBytes();
				} else {
					base64Payload = Base64.encodeBase64(encrypted);
				}

				// Publish a QoS message
				// It is not necessary publish topic. The message will be
				// received by the handler in server
				QoS qosLevel = ((MQTTConnectionConfig) config).getQualityOfService();
				if (log.isDebugEnabled()) {
					log.debug(String.format(
							"Sending MQTT PUBLISH message to the SIB server using the internal MQTT client %s. QoS=%s, Payload=%s.",
							mqttClientId, qosLevel, msg.toJson()));
				}
				mqttConnection.publish("", base64Payload, qosLevel, false);

			} finally {
				lock.unlock();
			}

			SSAPMessage ssapResponse = SSAPMessage.fromJsonToSSAPMessage(callback.get());
			if (log.isDebugEnabled()) {
				log.debug(String.format(
						"The internal MQTT client %s received a SSAP response from the SIB server. Payload=%s, Original request=%s.",
						mqttClientId, ssapResponse.toJson(), msg.toJson()));
			}
			return ssapResponse;

		} catch (Exception e) {
			String errorMessage = String.format("Unable to send SSAP message to the SIB server using internal MQTT client %s. Payload=%s.", 
					mqttClientId, msg.toJson());
			log.error(errorMessage, e);
			throw new ConnectionToSibException(errorMessage, e);
		}
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
	
	private void configureMqttClient(MQTTConnectionConfig cfg, String sibAddress)
			throws URISyntaxException, UnrecoverableKeyException, KeyManagementException, FileNotFoundException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		if (mqttClient == null) {
			mqttClient = new MQTT();
			String username = cfg.getUser();
			String password = cfg.getPassword();
			if (username != null) {
				mqttClient.setUserName(username);
			}
			if (password != null) {
				mqttClient.setPassword(password);
			}

			mqttClientId = buildMqttClientId(cfg);
			mqttClient.setClientId(mqttClientId);

			if (sibAddress.startsWith("ssl://")) {
				// Inicializar solo una vez
				mqttClient.setHost(sibAddress + ":" + config.getPortSIB());
				mqttClient.setSslContext(SSLContextHolder.getSSLContext());
			} else {
				mqttClient.setHost(sibAddress, config.getPortSIB());
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
		}
	}
	
	private String getSibAddress(MQTTConnectionConfig cfg){
		String sibAddress;
		try {
			if (config.getHostSIB().startsWith("tcp://") || config.getHostSIB().startsWith("ssl://")) {
				InetAddress.getByName(config.getHostSIB().substring(6));
			} else {
				InetAddress.getByName(config.getHostSIB());
			}
			sibAddress = config.getHostSIB();
			log.info(String.format("The SIB address %s can be resolved. The connection process will continue.",
					config.getHostSIB()));
		} catch (java.net.UnknownHostException e) {
			if (cfg.getDnsFailHostSIB() == null || cfg.getDnsFailHostSIB().trim().length() == 0) {
				String errorMessage = String.format(
						"The SIB address %s couldn't be resolved. The connection process has been aborted.",
						config.getHostSIB());
				log.error(errorMessage);
				internetConnectionTester.testConnection();
				throw new ConnectionToSibException(errorMessage);
			} else {
				sibAddress = cfg.getDnsFailHostSIB();
				log.warn(String.format("The SIB address %s couldn't be resolved. Using fallback IP address %s.",
						config.getHostSIB(), cfg.getDnsFailHostSIB()));
			}
		}
		return sibAddress;
	}
	
	private static String buildMqttClientId(MQTTConnectionConfig cfg){
		String mqttClientId;
		if (cfg.getClientId() == null) {
			mqttClientId = MQTTConnectionConfig.generateClientId();
		} else {
			if (cfg.getClientId().length() > MqttConstants.CLIENT_ID_LENGTH) {
				log.warn(String.format(
						"The MQTT client ID '%s' is too long. It has been trimmed to %s characters.",
						cfg.getClientId(), MqttConstants.CLIENT_ID_LENGTH));
				mqttClientId = cfg.getClientId().substring(0, MqttConstants.CLIENT_ID_LENGTH);
			} else {
				mqttClientId = cfg.getClientId();
			}
		}
		return mqttClientId;
	}
	
	private void subscribeToMqttTopic(String topicName) {
		Future<byte[]> subscribeFuture = null;

		// Subscription to topic for ssap response messages
		QoS qosLevel = ((MQTTConnectionConfig) config).getQualityOfService();
		try {
			log.info(String.format("Subscribing internal MQTT client %s to SIB topic %s with QoS=%s.", mqttClientId,
					topicName, qosLevel));
			Topic[] topics = { new Topic(topicName,  qosLevel)};
			subscribeFuture = mqttConnection.subscribe(topics);

			int timeout = config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			String errorMessage = String.format(
					"Unable to subscribe internal MQTT client %s to the SIB MQTT topic %s with QoS=%s. The connection process will be aborted.",
					mqttClientId, topicName, qosLevel);
			log.error(errorMessage, e);
			throw new ConnectionToSibException(errorMessage, e);
		}
	}
	
	private void unsubscribeFromMqttTopic(String publicationTopicName) {
		Future<Void> subscribeFuture = null;
		// Unsubscription to topic for ssap response messages
		try {
			log.info(String.format("Unsubscribing internal MQTT client %s from the SIB MQTT topic %s.",
					mqttClientId, publicationTopicName));
			String[] topics = { new String(publicationTopicName) };

			subscribeFuture = mqttConnection.unsubscribe(topics);
			int timeout = config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}

		} catch (Exception e) {
			log.warn(String.format("Unable to unsubscribe internal MQTT client %s from the SIB MQTT topic %s.",
					mqttClientId, publicationTopicName), e);
		}
	}
	
	/* 
	 * ********************************************************************
	 * Getters to be used by the MQTT callbacks and worker threads
	 * ********************************************************************
	 */
	
	String getMqttClientId(){
		return this.mqttClientId;
	}
	
	void setSessionKey(String sessionKey){
		this.sessionKey = sessionKey;
	}
	
	void setJoined(boolean joined){
		this.joined = joined;
	}
	
	List<Listener4SIBIndicationNotifications> getSubscriptionListeners(){
		return this.subscriptionListeners;
	}
	
	String getBaseCommandRequestSubscriptionId(){
		return this.baseCommandRequestSubscriptionId;
	}
	
	Listener4SIBIndicationNotifications getListener4BaseCommandRequestNotifications(){
		return this.listener4BaseCommandRequestNotifications;
	}
	
	String getStatusControlRequestSubscriptionId(){
		return this.statusControlRequestSubscriptionId;
	}
	
	Listener4SIBIndicationNotifications getListener4StatusControlRequestNotifications(){
		return listener4StatusControlRequestNotifications;
	}
	
	String getXxteaCipherKey(){
		return this.xxteaCipherKey;
	}
	
	FutureConnection getMqttConnection(){
		return mqttConnection;
	}
	
	MqttReceptionCallback getResponseCallback(){
		return responseCallback;
	}
	
	MqttSubscriptionThread getSubscriptionThread(){
		return subscriptionThread;
	}
	
	InternetConnectionTester getInternetConnectionTester(){
		return internetConnectionTester;
	}
	
	/* 
	 * ********************************************************************
	 * Auxiliary methods used by the MQTT callbacks and worker threads
	 * ********************************************************************
	 */
	
	void runIndicationTasks(Collection<IndicationTask> indicationTasks){
		super.executeIndicationTasks(indicationTasks);
	}
}