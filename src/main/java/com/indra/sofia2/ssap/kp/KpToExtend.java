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
package com.indra.sofia2.ssap.kp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.config.ConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.kp.utils.KpReporter;
import com.indra.sofia2.ssap.kp.utils.OsHwInfo;
import com.indra.sofia2.ssap.kp.utils.functions.Supplier;
import com.indra.sofia2.ssap.ssap.SSAPMessage;

public abstract class KpToExtend implements Kp {

	private static final Logger log = LoggerFactory.getLogger(KpToExtend.class);

	/**
	 * XXTEA algorithm cipher key
	 */
	protected String xxteaCipherKey;

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

	/**
	 * response timeout for all the SSAP messages sent to the SIB server
	 */
	protected static int ssapResponseTimeout;
	
	protected String kp;
	protected String kpInstance;
	protected String token;
	protected KpReporter statusReporter;
	private Supplier<Map<String, String>> customStatusSupplier;
	private int statusReportPeriod = 60000;
	private boolean reportingStatus;
	
	
	protected KpToExtend(ConnectionConfig config, final String kp, final String kpInstance, final String token) throws ConnectionConfigException {
		this.config = config;
		this.kp = kp;
		this.kpInstance = kpInstance;
		this.token = token;
		
		this.initializeStatusReport();
		
		config.validate();
	}
	
	

	public int getSsapResponseTimeout() {
		return ssapResponseTimeout;
	}

	public void setSsapResponseTimeout(int timeout) {
		ssapResponseTimeout = timeout;
	}

	public void setConnectionConfig(ConnectionConfig config) {
		this.config = config;
	}

	public void setXxteaCipherKey(String cipherKey) {
		this.xxteaCipherKey = cipherKey;
	}
	
	public String getKp() {
		return kp;
	}

	
	public String getKpInstance() {
		return kpInstance;
	}

	public String getToken() {
		return token;
	}

	public int getStatusReportPeriod() {
		return statusReportPeriod;
	}

	public void setStatusReportPeriod(int statusReportPeriod) {
		this.statusReportPeriod = statusReportPeriod;
		
		this.statusReporter.setPeriod(statusReportPeriod);
	}
	public boolean isReportingStatus() {
		return reportingStatus;
			  
	}
	public void enableStatusReport() {
		if(!reportingStatus) {
			reportingStatus = true;
			this.statusReporter.startReporting();
		}
	}
	
	public void disableStatusReport() {
		if(reportingStatus) {
			reportingStatus = false;
			this.statusReporter.stopReporting();
		}
			
	}
	
	public Supplier<Map<String, String>> getCustomStatusSupplier() {
		return customStatusSupplier;
	}

	public synchronized void setCustomStatusSupplier(Supplier<Map<String, String>> customStatusSupplier) {
		this.customStatusSupplier = customStatusSupplier;
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
	
	protected void executeIndicationTasks(Collection<IndicationTask> indicationTasks) {
		for (IndicationTask task : indicationTasks) {
			this.indicationThreadPool.submit(task);
		}
	}

	protected void initializeIndicationPool() {
		log.info("Initializing SSAP INDICATION listener thread pool.");
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
					if (connectionEventsListener != null) {
						// This IF prevents a NullPointerException when several threads call this method
						connectionEventsListener.notifyConnection();
					}
				}
			}.start();
		}
	}

	protected void notifyDisconnectionEvent() {
		if (this.connectionEventsListener != null) {
			new Thread() {
				public void run() {
					if (connectionEventsListener != null) {
						// This IF prevents a NullPointerException when several threads call this method
						connectionEventsListener.notifyDisconnection();
					}
				}
			}.start();
		}
	}
	
	private void initializeStatusReport() {
		
		//Report stauts periodically
		statusReporter = new KpReporter(this, statusReportPeriod, new Supplier<SSAPMessage>() {
			@Override
			public SSAPMessage get() {
				return getStatusMessage();
			}
		}).startReporting();
		reportingStatus = true;
		
		
	}
	
	private SSAPMessage getStatusMessage() {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		Map<String,String> status = new HashMap<String, String>();	
		
		status.putAll(OsHwInfo.getFullStatus());
		if(customStatusSupplier != null)
			status.putAll(customStatusSupplier.get());
		
		SSAPMessage msgStatus=SSAPMessageGenerator.getInstance().generateStatusMessage(kp, kpInstance, token, status, timeStamp);
		return msgStatus;
	}
}
