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
package com.indra.sofia2.ssap.kp.implementations.rest.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "config_response")
@XmlType(name = "config_response")
public class ConfigResponse extends CommonResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String results;

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static ConfigResponse fromJsonToConfigResponse(String json) {
		return new JSONDeserializer<ConfigResponse>().use(null,
				ConfigResponse.class).deserialize(json);
	}

	public static String toJsonArray(Collection<ConfigResponse> collection) {
		return new JSONSerializer().exclude("*.class").serialize(collection);
	}

	public static String toJsonArray(Collection<ConfigResponse> collection,
			String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(collection);
	}

	public static Collection<ConfigResponse> fromJsonArrayToConfigResponses(
			String json) {
		return new JSONDeserializer<List<ConfigResponse>>()
				.use(null, ArrayList.class).use("values", ConfigResponse.class)
				.deserialize(json);
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
