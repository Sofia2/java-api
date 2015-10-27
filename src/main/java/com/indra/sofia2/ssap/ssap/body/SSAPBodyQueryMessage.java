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

public class SSAPBodyQueryMessage extends SSAPBodyMessage {

	/*
	 * Sended query
	 */
	private String query;

	/*
	 * Type message
	 */
	private SSAPQueryType queryType;

	public void setQuery(String query) {
		this.query = prepareQuotes(query);
	}

	public void setQueryType(SSAPQueryType queryType) {
		this.queryType = queryType;
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
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBodyQueryMessage fromJsonToSSAPBodyQueryMessage(
			String json) {
		return new JSONDeserializer<SSAPBodyQueryMessage>().use(null,
				SSAPBodyQueryMessage.class).deserialize(json);
	}

	public static Collection<SSAPBodyQueryMessage> fromJsonArrayToSSAPBodyQueryMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyQueryMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyQueryMessage.class).deserialize(json);
	}

}
