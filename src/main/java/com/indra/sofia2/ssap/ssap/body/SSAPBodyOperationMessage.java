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

import com.indra.sofia2.ssap.ssap.SSAPQueryType;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodyOperationMessage extends SSAPBodyMessage {

	/*
	 * Mensaje enviado
	 */
	private String data;

	/*
	 * Query enviada
	 */
	private String query;

	private SSAPQueryType queryType;

	public void setData(String datos) {
		this.data = prepareQuotes(datos);
	}

	public void setQuery(String query) {
		this.query = prepareQuotes(query);
	}

	public void setQueryType(SSAPQueryType queryType) {
		this.queryType = queryType;
	}

	public String getData() {
		return data;
	}

	public String getQuery() {
		return query;
	}

	public SSAPQueryType getQueryType() {
		return queryType;
	}

	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class").serialize(this);
	}

	public static SSAPBodyOperationMessage fromJsonToSSAPBodyOperationMessage(String json) {
		return new JSONDeserializer<SSAPBodyOperationMessage>().use(null,
				SSAPBodyOperationMessage.class).deserialize(json);
	}

	public static String toJson(SSAPBodyOperationMessage body) {
		return new JSONSerializer().exclude("*.class").serialize(body);
	}

	public static Collection<SSAPBodyOperationMessage> fromJsonArrayToSSAPBodyOperationMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyOperationMessage>>().use(null, ArrayList.class)
				.use("values", SSAPBodyOperationMessage.class).deserialize(json);
	}

}
