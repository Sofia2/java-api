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

import org.atmosphere.wasync.Request;

import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;

public class WebSocketConnectionConfig extends ConnectionConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Request.METHOD method=Request.METHOD.GET;
	
	private String endpointUri;
	
	private int keepAliveInSeconds = 60;
	
	public void validateConfig() throws ConnectionConfigException{
		if (endpointUri == null || endpointUri.trim().length()==0) {
			throw new ConnectionConfigException("endpointUri cannot be empty or null"); 
		}
	}
	
	public Request.METHOD getMethod() {
		return method;
	}
	public void setMethod(Request.METHOD method) {
		this.method = method;
	}
	public String getEndpointUri() {
		return endpointUri;
	}
	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public int getKeepAliveInSeconds() {
		return keepAliveInSeconds;
	}

	public void setKeepAliveInSeconds(int keepAliveInSeconds) {
		this.keepAliveInSeconds = keepAliveInSeconds;
	}
	
	
}
