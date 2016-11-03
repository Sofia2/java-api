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
package com.indra.sofia2.ssap.kp.implementations.rest.response;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.kp.exceptions.SSAPMessageDeserializationError;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "config_response")
@XmlType(name = "config_response")
public class ConfigResponse extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String results;

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJsonArray(Collection<ConfigResponse> collection) {
		try {
			return new ObjectMapper().writeValueAsString(collection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ConfigResponse fromJsonToConfigResponse(String json) {
		try {
			return new ObjectMapper().readValue(json, ConfigResponse.class);
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}

	public static Collection<ConfigResponse> fromJsonArrayToConfigResponses(String json) {
		try {
			return new ObjectMapper().readValue(json, new TypeReference<Collection<ConfigResponse>>(){});
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
