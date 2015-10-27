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
package com.indra.sofia2.ssap.kp.implementations.tcpip.connector;

import java.util.Properties;

import com.indra.sofia2.ssap.kp.implementations.tcpip.exception.KPIConnectorException;



public abstract class AbstractConnector {
	
	protected ConnectorMessageSupport messageListeners;
	protected ConnectionSupport connectionListeners;
	protected Properties properties;
	
	/** Indicates if the connector must keep the connection open after sending a message*/
	protected boolean keepAlive;
	
	public AbstractConnector() {
		messageListeners = new ConnectorMessageSupport();
		connectionListeners = new ConnectionSupport();
	}
	
	public abstract void write(byte[] msg);
	
	public abstract String getName();
	public abstract void connect() throws KPIConnectorException;
	public abstract void disconnect() throws KPIConnectorException;
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return this.properties;
	}
	
	public String getProperty(String key) {
		if(this.properties == null) {
			return null;
		} else {
			return this.properties.getProperty(key);
		}
	}
	
	public void addSIBMessageListener(IConnectorMessageListener listener) {
		messageListeners.addListener(listener);
	}

	public void removeSIBMessageListener(IConnectorMessageListener listener) {
		messageListeners.removeListener(listener);
	}
	
	public void addConnectionListener(IConnectionListener listener) {
		connectionListeners.addListener(listener);
	}

	public void removeConnectionListener(IConnectionListener listener) {
		connectionListeners.removeListener(listener);
	}
	
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean keepAlive() {
		return this.keepAlive;
	}
	

}
