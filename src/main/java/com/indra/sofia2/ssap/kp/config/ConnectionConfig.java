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

import java.io.Serializable;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;

public class ConnectionConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sibHost;
	private int sibPort;
	private int sibConnectionTimeout;
	private int subscriptionListenersPoolSize;

	public ConnectionConfig() {
		this.sibHost = null;
		this.sibPort = 0;
		this.sibConnectionTimeout = 5000;
		this.subscriptionListenersPoolSize = 1;
	}

	public String getSibHost() {
		return sibHost;
	}

	public void setSibHost(String sibHost) {
		this.sibHost = sibHost;
	}

	public int getSibPort() {
		return sibPort;
	}

	public void setSibPort(int sibPort) {
		this.sibPort = sibPort;
	}

	public int getSibConnectionTimeout() {
		return sibConnectionTimeout;
	}

	public void setSibConnectionTimeout(int sibConnectionTimeout) {
		this.sibConnectionTimeout = sibConnectionTimeout;
	}

	public int getSubscriptionListenersPoolSize() {
		return subscriptionListenersPoolSize;
	}

	public void setSubscriptionListenersPoolSize(int subscriptionListenersPoolSize) {
		this.subscriptionListenersPoolSize = subscriptionListenersPoolSize;
	}

	public void validate() throws ConnectionConfigException {
		if (sibHost == null) {
			throw new ConnectionConfigException("The SIB host is required");
		}
		if (sibPort == 0) {
			throw new ConnectionConfigException("The SIB port is required");
		}
	}
}
