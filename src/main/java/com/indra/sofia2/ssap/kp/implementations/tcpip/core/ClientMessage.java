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
package com.indra.sofia2.ssap.kp.implementations.tcpip.core;

public class ClientMessage {
	private static long idGenerator;
	
	private long id;
	private long timeStamp;
	private byte[] payload;
	
	public ClientMessage() {
		// No need to synchronize because ++ is a one cycle operation.
		id = idGenerator++;
		timeStamp = System.nanoTime();
	}
	
	public ClientMessage(byte[] payload) {
		this();
		this.payload = payload;
	}

	public long getId() {
		return id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public int getPayloadSize() {
		if(payload == null) {
			return 0;
		} else {
			return payload.length;
		}
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: ");
		sb.append(id);
		sb.append(", timestamp: ");
		sb.append(timeStamp);
		sb.append(", len: ");
		sb.append(payload.length);
		sb.append(", content:\n");
		sb.append(new String(payload));
		return sb.toString();
	}
}

