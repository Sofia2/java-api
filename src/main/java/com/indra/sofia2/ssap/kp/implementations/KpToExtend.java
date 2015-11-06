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
package com.indra.sofia2.ssap.kp.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.indra.sofia2.ssap.kp.ConnectionListener;
import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.Listener4SIBCommandMessageNotifications;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.ConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.implementations.tcpip.connector.IConnectorMessageListener;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;

public abstract class KpToExtend implements Kp, IConnectorMessageListener {

	protected static Log log = LogFactory.getLog(KpToExtend.class);

	/**
	 * Session key del kp en la conexión con el SIB
	 */
	protected String sessionKey;

	/**
	 * XXTEA algorithm cipher key
	 */
	protected String xxteaCipherKey = null;

	/**
	 * Subscription listeners thread pool
	 */

	private ExecutorService indicationThreadPool;

	/**
	 * Registra los listeners para mensaje de notificacion del Sib
	 */
	protected List<Listener4SIBIndicationNotifications> subscriptionListeners = new ArrayList<Listener4SIBIndicationNotifications>();

	/**
	 * Registers listeners for raw messages from SIB notifications
	 */
	protected List<Listener4SIBCommandMessageNotifications> subscriptionCommandMessagesListener = new ArrayList<Listener4SIBCommandMessageNotifications>();

	/**
	 * Listeners for notifications of Base Commands
	 */
	protected Listener4SIBIndicationNotifications listener4BaseCommandRequestNotifications;

	/**
	 * Listeners for notifications of Status request
	 */
	protected Listener4SIBIndicationNotifications listener4StatusControlRequestNotifications;

	/**
	 * ConnectionListener
	 */
	private ConnectionListener connectionEventsListener;

	/**
	 * Estado conexion
	 */
	protected boolean joined = false;

	/**
	 * Configuracion de Conexion
	 */
	protected ConnectionConfig config = null;

	/**
	 * subscriptionId for BaseCommandRequest notifications
	 */
	protected String baseCommandRequestSubscriptionId;

	/**
	 * subscriptionId for StatusCommandRequest notifications
	 */
	protected String statusControlRequestSubscriptionId;

	protected static int ssapResponseTimeout;

	public int getSsapResponseTimeout() {
		return ssapResponseTimeout;
	}

	public void setSsapResponseTimeout(int timeout) {
		ssapResponseTimeout = timeout;
	}

	/**
	 * Hace la desconexion del protocolo físico
	 */
	public void setConnectionConfig(ConnectionConfig config) {
		this.config = config;
	}

	public KpToExtend(ConnectionConfig config) throws ConnectionConfigException {
		this.config = config;
		if (config == null) {
			throw new ConnectionConfigException("Configuration is null");
		}
		config.validateConfig();
	}

	public boolean isJoined() {
		return this.joined;
	}

	@Override
	public String getSessionKey() {
		return this.sessionKey;
	}

	public void setXxteaCipherKey(String cipherKey) {
		this.xxteaCipherKey = cipherKey;
	}

	@Override
	public void addListener4SIBNotifications(Listener4SIBIndicationNotifications listener) {
		subscriptionListeners.add(listener);
	}

	@Override
	public void removeListener4SIBNotifications() {
		subscriptionListeners.clear();
	}

	@Override
	public void removeListener4SIBNotifications(Listener4SIBIndicationNotifications listener) {
		subscriptionListeners.remove(listener);
	}

	@Override
	public void addListener4SIBCommandMessageNotifications(Listener4SIBCommandMessageNotifications listener) {
		subscriptionCommandMessagesListener.add(listener);
	}

	@Override
	public void removeListener4SIBCommandMessageNotifications(Listener4SIBCommandMessageNotifications listener) {
		subscriptionCommandMessagesListener.remove(listener);
	}

	@Override
	public void setListener4BaseCommandRequestNotifications(Listener4SIBIndicationNotifications listener) {
		listener4BaseCommandRequestNotifications = listener;
	}

	@Override
	public void setListener4StatusControlRequestNotifications(Listener4SIBIndicationNotifications listener) {
		listener4StatusControlRequestNotifications = listener;
	}

	@Override
	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionEventsListener = connectionListener;
	}

	@Override
	public void removeConnectionListener() {
		this.connectionEventsListener = null;
	}
	
	@Override
	public void messageReceived(byte[] message) {
	}

	protected void executeIndicationTasks(Collection<IndicationTask> indicationTasks) {
		for (IndicationTask task : indicationTasks) {
			this.indicationThreadPool.submit(task);
		}
	}

	protected void initializeIndicationPool() {
		log.info("Initializing INDICATION listener thread pool.");
		this.indicationThreadPool = Executors.newFixedThreadPool(config.getSubscriptionListenersPoolSize());
	}

	protected void destroyIndicationPool() {
		if (this.indicationThreadPool != null && !this.indicationThreadPool.isShutdown()) {
			try {
				this.indicationThreadPool.shutdown();
			} catch (Throwable e) {
				log.error("Unable to stop indication thread pool.", e);
				try {
					this.indicationThreadPool.shutdownNow();
				} catch (Throwable e1) {
					log.error("Unable to stop pending tasks. These tasks will end according to their code.", e1);
				}
			}
		}
		this.indicationThreadPool = null;
	}

	protected void notifyConnectionEvent() {
		if (this.connectionEventsListener != null) {
			new Thread() {
				public void run() {
					connectionEventsListener.notifyConnection();
				}

			}.start();
		}
	}

	protected void notifyDisconnectionEvent() {
		if (this.connectionEventsListener != null) {
			new Thread() {
				public void run() {
					connectionEventsListener.notifyDisconnection();
				}
			}.start();
		}
	}
}
