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
package com.indra.sofia2.ssap.ssap.body.bulk.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBulkOperationSummary {
	
	private List<String> objectIds;
	private List<SSAPBulkOperationErrorSummary> errors;
	
	public List<String> getObjectIds() {
		return objectIds;
	}
	public void setObjectIds(List<String> objectIds) {
		this.objectIds = objectIds;
	}
	public List<SSAPBulkOperationErrorSummary> getErrors() {
		return errors;
	}
	public void setErrors(List<SSAPBulkOperationErrorSummary> errors) {
		this.errors = errors;
	}
	
	public String toJson() {
		return new JSONSerializer().include("objectIds").include("errors").exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include("objectIds").include("errors").include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBulkOperationErrorSummary fromJsonToSSAPBulkOperationErrorSummary(
			String json) {
		return new JSONDeserializer<SSAPBulkOperationErrorSummary>().use(null,
				SSAPBulkOperationErrorSummary.class).deserialize(json);
	}

	public static String toJsonArray(
			Collection<SSAPBulkOperationErrorSummary> collection) {
		return new JSONSerializer().include("objectIds").include("errors").exclude("*.class").serialize(collection);
	}

	public static String toJsonArray(
			Collection<SSAPBulkOperationErrorSummary> collection, String[] fields) {
		return new JSONSerializer().include("objectIds").include("errors").include(fields).exclude("*.class")
				.serialize(collection);
	}

	public static Collection<SSAPBulkOperationErrorSummary> fromJsonArrayToSSAPBulkOperationErrorSummarys(
			String json) {
		return new JSONDeserializer<List<SSAPBulkOperationErrorSummary>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBulkOperationErrorSummary.class)
				.deserialize(json);
	}
	

}
