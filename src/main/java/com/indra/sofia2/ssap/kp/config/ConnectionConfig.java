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

import java.io.Serializable;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;

public class ConnectionConfig implements Serializable {

	private String hostSIB;

	private int portSIB;

	private int timeOutConnectionSIB;

	private int subscriptionListenersPoolSize;
	private static final long serialVersionUID = 1L;
	
	public ConnectionConfig() {
		this.hostSIB = null;
		this.portSIB = 0;
		this.timeOutConnectionSIB = 5000;
		this.subscriptionListenersPoolSize = 1;
	}

	public String getHostSIB() {
		return hostSIB;
	}

	public int getPortSIB() {
		return portSIB;
	}

	public int getSubscriptionListenersPoolSize() {
		return subscriptionListenersPoolSize;
	}

	public int getTimeOutConnectionSIB() {
		return timeOutConnectionSIB;
	}

	public void setHostSIB(String hostSIB) {
		this.hostSIB = hostSIB;
	}

	public void setPortSIB(int portSIB) {
		this.portSIB = portSIB;
	}

	public void setSubscriptionListenersPoolSize(
			int subscriptionListenersPoolSize) {
		this.subscriptionListenersPoolSize = subscriptionListenersPoolSize;
	}

	public void setTimeOutConnectionSIB(int timeOutConnectionSIB) {
		this.timeOutConnectionSIB = timeOutConnectionSIB;
	}

	public void validateConfig() throws ConnectionConfigException {
		if (hostSIB == null) {
			throw new ConnectionConfigException(
					"Host Connection to SIB not established");
		}
		if (portSIB == 0) {
			throw new ConnectionConfigException(
					"Post Connection to SIB not established");
		}
	}

}
