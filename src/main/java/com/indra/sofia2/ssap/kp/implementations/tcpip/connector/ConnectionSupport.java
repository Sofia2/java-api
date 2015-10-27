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

import com.indra.sofia2.ssap.kp.implementations.tcpip.common.AbstractSupport;


public class ConnectionSupport extends AbstractSupport<IConnectionListener> {
	
	public ConnectionSupport() {
		super();
	}
  
	public void fireConnectionClosed(AbstractConnector connector) {
		synchronized(listeners) {
			for(IConnectionListener listener : listeners) {
				listener.connectionClosed(connector);
			}
		}
	}

	public void fireConnectionError(AbstractConnector connector, String error) {
		synchronized(listeners) {
			for(IConnectionListener listener : listeners) {
				listener.connectionError(connector, error);
			}
		}
	}

	public void fireConnectionTimeout(AbstractConnector connector) {
		synchronized(listeners) {
			for(IConnectionListener listener : listeners) {
				listener.connectionTimeout(connector);
			}
		}
	}

}
