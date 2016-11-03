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
package com.indra.sofia2.ssap.ssap.body;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementacion del mensaje JoinMessage con token
 * 
 * @author jfgpimpollo
 * 
 */
public class SSAPBodyJoinTokenMessage extends SSAPBodyJoinMessage {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPBodyJoinTokenMessage fromJsonToSSAPBodyJoinTokenMessage(String json) throws IOException {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.enableDefaultTyping();
		return objMapper.readValue(json, SSAPBodyJoinTokenMessage.class);
	}
}
