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
package com.indra.sofia2.ssap.ssap.body;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Implementacion de JoinMessage con usuario y password
 * 
 * @author lmgracia
 * 
 */
public class SSAPBodyJoinUserAndPasswordMessage extends SSAPBodyJoinMessage {

	/*
	 * Usuario para logarse en el sistema
	 */
	private String user;
	/*
	 * Password para logarse en el sistema.
	 */
	private String password;

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

	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBodyJoinUserAndPasswordMessage fromJsonToSSAPBodyJoinUserAndPasswordMessage(
			String json) {
		return new JSONDeserializer<SSAPBodyJoinUserAndPasswordMessage>().use(
				null, SSAPBodyJoinUserAndPasswordMessage.class).deserialize(
				json);
	}

	public static Collection<SSAPBodyJoinUserAndPasswordMessage> fromJsonArrayToSSAPBodyJoinUserAndPasswordMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyJoinUserAndPasswordMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyJoinUserAndPasswordMessage.class)
				.deserialize(json);
	}

}
