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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodyQueryWithParamMessage extends SSAPBodyOperationMessage {

	/*
	 * Params query key-value
	 */
	private Map<String, String> queryParams;

	public void setQueryParams(Map<String, String> params) {
		this.queryParams = params;
	}

	public Map<String, String> getQueryParams() {
		return queryParams;
	}

	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBodyQueryWithParamMessage fromJsonToSSAPBodyQueryWithParamMessage(
			String json) {
		return new JSONDeserializer<SSAPBodyQueryWithParamMessage>().use(null,
				SSAPBodyQueryWithParamMessage.class).deserialize(json);
	}

	public static Collection<SSAPBodyQueryWithParamMessage> fromJsonArrayToSSAPBodyQueryWithParamMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyQueryWithParamMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyQueryWithParamMessage.class)
				.deserialize(json);
	}

}
