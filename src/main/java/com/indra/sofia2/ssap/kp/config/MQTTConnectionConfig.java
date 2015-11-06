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
package com.indra.sofia2.ssap.kp.config;

import java.util.UUID;

import org.fusesource.mqtt.client.QoS;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.implementations.mqtt.MqttConstants;

public class MQTTConnectionConfig extends ConnectionConfig {
	
	public int getSendBufferSize() {
		return sendBufferSize;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Sets the quality of service to use for the Will message. Defaults to QoS.AT_MOST_ONCE.
	 */
	private QoS qualityOfService=QoS.AT_MOST_ONCE;

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
	 * Set to false if you want the MQTT server to persist topic subscriptions and ack positions across client sessions. Defaults to true.
	 */
	private boolean cleanSession=false;
	/**
	 * Configures the Keep Alive timer in seconds. Defines the maximum time interval between messages received from a client. 
	 * It enables the server to detect that the network connection to a client has dropped, without having to wait for the long TCP/IP timeout.
	 */
	private int keepAliveInSeconds=0;
	
	/**
	 * Timeout to receive a SSAP response in milliseconds.
	 */
	private int ssapResponseTimeout=5000;
	
	/**
	 * The maximum number of reconnect attempts before an error is reported back to 
	 * the client on the first attempt by the client to connect to a server. 
	 * Set to -1 to use unlimited attempts. Defaults to -1.
	 */
	private int connectAttemptsMax;
	
	/**
	 * The maximum number of reconnect attempts before an error is reported back to 
	 * the client after a server connection had previously been established. 
	 * Set to -1 to use unlimited attempts. Defaults to -1.
	 */
	private int reconnectAttemptsMax;
	
	/**
	 * How long to wait in ms before the first reconnect attempt. Defaults to 10.
	 */
	private long reconnectDelay;
	
	/**
	 * The maximum amount of time in ms to wait between reconnect attempts. 
	 * Defaults to 30,000.
	 */
	private long reconnectDelayMax;
	
	/**
	 * The Exponential backoff to be used between reconnect attempts. 
	 * Set to 1 to disable exponential backoff. Defaults to 2.
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
	 * The traffic class or type-of-service octet in the IP header for packets sent 
	 * from the transport. Defaults to 8 which means the traffic should be optimized 
	 * for throughput.
	 */
	private int trafficClass;
	
	/**
	 * The maximum bytes per second that this client will receive data at. 
	 * This setting throttles reads so that the rate is not exceeded. Defaults 
	 * to 0 which disables throttling.
	 */
	private int maxReadRate;
	
	/**
	 * Sets the maximum bytes per second that this client will send data at. 
	 * This setting throttles writes so that the rate is not exceeded. 
	 * Defaults to 0 which disables throttling.
	 */
	private int maxWriteRate;
	
	public MQTTConnectionConfig(){
		this.connectAttemptsMax = 0;
		this.reconnectAttemptsMax = 0;
		this.reconnectDelay = 10;
		this.reconnectDelayMax = 30000;
		this.reconnectBackOffMultiplier = 2;
		this.receiveBufferSize = 65536;
		this.sendBufferSize = 65536;
		this.trafficClass = 8;
		this.maxReadRate = 0;
		this.maxWriteRate = 0;
	}
	
	public int getMaxReadRate() {
		return maxReadRate;
	}

	public int getMaxWriteRate() {
		return maxWriteRate;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public int getTrafficClass() {
		return trafficClass;
	}

	public void validateConfig() throws ConnectionConfigException{
		super.validateConfig();
		if (qualityOfService == null) {
			throw new ConnectionConfigException("Quality Of Service not established"); 
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
	
	
	public static String generateClientId() {
		return UUID.randomUUID().toString().substring(0, MqttConstants.CLIENT_ID_LENGTH);
	}
	
	public int getConnectAttemptsMax() {
		return connectAttemptsMax;
	}

	public void setConnectAttemptsMax(int connectAttemptsMax) {
		this.connectAttemptsMax = connectAttemptsMax;
	}

	public int getReconnectAttemptsMax() {
		return reconnectAttemptsMax;
	}

	public void setReconnectAttemptsMax(int reconnectAttemptsMax) {
		this.reconnectAttemptsMax = reconnectAttemptsMax;
	}

	public long getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(long reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public long getReconnectDelayMax() {
		return reconnectDelayMax;
	}

	public void setReconnectDelayMax(long reconnectDelayMax) {
		this.reconnectDelayMax = reconnectDelayMax;
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
	
	
}