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
package com.indra.sofia2.ssap.ssap.body.bulk.message;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;

public class SSAPBodyBulkItem {
	
	private SSAPMessageTypes type;
	private String body;
	private String ontology;
	
	public SSAPMessageTypes getType() {
		return type;
	}
	
	public void setType(SSAPMessageTypes type) {
		this.type = type;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJsonArray(
			Collection<SSAPBodyBulkItem> collection) {
		try {
			return new ObjectMapper().writeValueAsString(collection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPBodyBulkItem fromJsonToSSAPBodyBulkItem(String json) throws IOException {
		return new ObjectMapper().readValue(json, SSAPBodyBulkItem.class);
	}

	public static Collection<SSAPBodyBulkItem> fromJsonArrayToSSAPBodyBulkItems(String json) throws IOException {
		return new ObjectMapper().readValue(json, new TypeReference<Collection<SSAPBodyBulkItem>>(){});
	}

}
