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
package com.indra.sofia2.ssap.kp.config;

import java.util.UUID;

import org.fusesource.mqtt.client.QoS;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.implementations.mqtt.MqttConstants;

public class MQTTConnectionConfig extends ConnectionConfig {

	private static final long serialVersionUID = 1L;

	/**
	 * Sets the quality of service to use for the Will message. Defaults to
	 * QoS.AT_MOST_ONCE.
	 */
	private QoS qualityOfService = QoS.AT_MOST_ONCE;

	/**
	 * Sets an IP to connect the KP in case of DNS resolver fails
	 */
	private String dnsFailHostSIB;

	/**
	 * Sets the user name used to authenticate against the server.
	 */
	private String user;

	/**
	 * Sets the password used to authenticate against the server.
	 */
	private String password;

	/**
	 * ClientId
	 */
	private String clientId;

	/**
	 * Set to false if you want the MQTT server to persist topic subscriptions
	 * and ack positions across client sessions. Defaults to true.
	 */
	private boolean cleanSession = false;
	/**
	 * Configures the Keep Alive timer in seconds. Defines the maximum time
	 * interval between messages received from a client. It enables the server
	 * to detect that the network connection to a client has dropped, without
	 * having to wait for the long TCP/IP timeout.
	 */
	private int keepAliveInSeconds = 0;

	/**
	 * Timeout to receive a SSAP response in milliseconds.
	 */
	private int ssapResponseTimeout = 5000;

	/**
	 * The maximum number of reconnect attempts before an error is reported back
	 * to the client on the first attempt by the client to connect to a server.
	 * Set to -1 to use unlimited attempts. Defaults to -1.
	 */
	private int maxConnectAttempts;

	/**
	 * The maximum number of reconnect attempts before an error is reported back
	 * to the client after a server connection had previously been established.
	 * Set to -1 to use unlimited attempts. Defaults to -1.
	 */
	private int maxReconnectAttempts;

	/**
	 * How long to wait in ms before the first reconnect attempt. Defaults to
	 * 10.
	 */
	private long reconnectDelay;

	/**
	 * The maximum amount of time in ms to wait between reconnect attempts.
	 * Defaults to 30,000.
	 */
	private long maxReconnectDelay;

	/**
	 * The Exponential backoff to be used between reconnect attempts. Set to 1
	 * to disable exponential backoff. Defaults to 2.
	 */
	private double reconnectBackOffMultiplier;

	/**
	 * The size of the internal socket receive buffer. Defaults to 65536 (64k)
	 */
	private int receiveBufferSize;

	/**
	 * The size of the internal socket send buffer. Defaults to 65536 (64k)
	 */
	private int sendBufferSize;

	/**
	 * The traffic class or type-of-service octet in the IP header for packets
	 * sent from the transport. Defaults to 8 which means the traffic should be
	 * optimized for throughput.
	 */
	private int trafficClass;

	/**
	 * The maximum bytes per second that this client will receive data at. This
	 * setting throttles reads so that the rate is not exceeded. Defaults to 0
	 * which disables throttling.
	 */
	private int maxReadRate;

	/**
	 * Sets the maximum bytes per second that this client will send data at.
	 * This setting throttles writes so that the rate is not exceeded. Defaults
	 * to 0 which disables throttling.
	 */
	private int maxWriteRate;

	/**
	 * Runs a DNS and an internet connectivity test before any connection
	 * attempt.
	 */
	private boolean checkInternetConnection;

	public MQTTConnectionConfig() {
		this.maxConnectAttempts = 0;
		this.maxReconnectAttempts = 0;
		this.reconnectDelay = 10;
		this.maxReconnectDelay = 30000;
		this.reconnectBackOffMultiplier = 2;
		this.receiveBufferSize = 65536;
		this.sendBufferSize = 65536;
		this.trafficClass = 8;
		this.maxReadRate = 0;
		this.maxWriteRate = 0;
		this.checkInternetConnection = false;
		this.clientId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, MqttConstants.CLIENT_ID_LENGTH);
		this.qualityOfService = QoS.AT_LEAST_ONCE;
	}

	public int getMaxReadRate() {
		return maxReadRate;
	}

	public int getMaxWriteRate() {
		return maxWriteRate;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public int getTrafficClass() {
		return trafficClass;
	}

	public void validate() throws ConnectionConfigException {
		super.validate();
		if (qualityOfService == null) {
			throw new ConnectionConfigException("The QoS level is required");
		}
	}

	public QoS getQualityOfService() {
		return qualityOfService;
	}

	public void setQualityOfService(QoS qualityOfService) {
		this.qualityOfService = qualityOfService;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	public int getKeepAliveInSeconds() {
		return keepAliveInSeconds;
	}

	public void setKeepAliveInSeconds(int keepAliveInSeconds) {
		this.keepAliveInSeconds = keepAliveInSeconds;
	}

	public int getSsapResponseTimeout() {
		return ssapResponseTimeout;
	}

	public void setSsapResponseTimeout(int ssapResponseTimeout) {
		this.ssapResponseTimeout = ssapResponseTimeout;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void resetClientId() {
		this.clientId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, MqttConstants.CLIENT_ID_LENGTH);
	}

	public int getConnectAttemptsMax() {
		return maxConnectAttempts;
	}

	public void setConnectAttemptsMax(int connectAttemptsMax) {
		this.maxConnectAttempts = connectAttemptsMax;
	}

	public int getReconnectAttemptsMax() {
		return maxReconnectAttempts;
	}

	public void setReconnectAttemptsMax(int reconnectAttemptsMax) {
		this.maxReconnectAttempts = reconnectAttemptsMax;
	}

	public long getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public long getReconnectDelayMax() {
		return maxReconnectDelay;
	}

	public void setReconnectDelayMax(long reconnectDelayMax) {
		this.maxReconnectDelay = reconnectDelayMax;
	}

	public double getReconnectBackOffMultiplier() {
		return reconnectBackOffMultiplier;
	}

	public void setReconnectBackOffMultiplier(double reconnectBackOffMultiplier) {
		this.reconnectBackOffMultiplier = reconnectBackOffMultiplier;
	}

	public String getDnsFailHostSIB() {
		return dnsFailHostSIB;
	}

	public void setDnsFailHostSIB(String dnsFailHostSIB) {
		this.dnsFailHostSIB = dnsFailHostSIB;
	}

	public boolean isCheckInternetConnection() {
		return checkInternetConnection;
	}

	public void setCheckInternetConnection(boolean checkInternetConnection) {
		this.checkInternetConnection = checkInternetConnection;
	}
}