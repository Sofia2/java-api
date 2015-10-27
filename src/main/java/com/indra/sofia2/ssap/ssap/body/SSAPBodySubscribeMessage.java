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
import java.util.Map;

import com.indra.sofia2.ssap.ssap.SSAPQueryType;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodySubscribeMessage extends SSAPBodyMessage {

	/*
	 * Tiempo para el reenvio de la subscripcion en ms
	 */
	private int msRefresh = 0;

	/*
	 * Query enviada
	 */
	private String query;

	/*
	 * Type message
	 */
	private SSAPQueryType queryType;

	/*
	 * Params query key-value
	 */
	private Map<String, String> queryParams;

	public void setQueryParams(Map<String, String> params) {
		this.queryParams = params;
	}

	public Map<String, String> getQueryParams() {
		return this.queryParams;
	}

	public void setQuery(String query) {
		if (queryType == SSAPQueryType.NATIVE) {
			this.query = prepareQuotes(query);
		} else {
			this.query = query;
		}
	}

	public void setQueryType(SSAPQueryType queryType) {
		this.queryType = queryType;
	}

	public int getMsRefresh() {
		return msRefresh;
	}

	public void setMsRefresh(int msRefresh) {
		this.msRefresh = msRefresh;
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

	public static SSAPBodySubscribeMessage fromJsonToSSAPBodySubscribeMessage(
			String json) {
		return new JSONDeserializer<SSAPBodySubscribeMessage>().use(null,
				SSAPBodySubscribeMessage.class).deserialize(json);
	}

	public static Collection<SSAPBodySubscribeMessage> fromJsonArrayToSSAPBodySubscribeMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodySubscribeMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodySubscribeMessage.class)
				.deserialize(json);
	}

}
