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

public class SSAPBodyBulkReturnMessage {
	
	private SSAPBulkOperationSummary insertSummary;
	private SSAPBulkOperationSummary updateSummary;
	private SSAPBulkOperationSummary deleteSummary;
	
	public SSAPBulkOperationSummary getInsertSummary() {
		return insertSummary;
	}
	public void setInsertSummary(SSAPBulkOperationSummary insertSummary) {
		this.insertSummary = insertSummary;
	}
	public SSAPBulkOperationSummary getUpdateSummary() {
		return updateSummary;
	}
	public void setUpdateSummary(SSAPBulkOperationSummary updateSummary) {
		this.updateSummary = updateSummary;
	}
	public SSAPBulkOperationSummary getDeleteSummary() {
		return deleteSummary;
	}
	public void setDeleteSummary(SSAPBulkOperationSummary deleteSummary) {
		this.deleteSummary = deleteSummary;
	}
	public String toJson() {
		return new JSONSerializer().include("insertSummary.objectIds").include("insertSummary.errors")
				.include("updateSummary.objectIds").include("updateSummary.errors")
				.include("deleteSummary.objectIds").include("deleteSummary.errors")
				.exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include("insertSummary.objectIds").include("insertSummary.errors")
				.include("updateSummary.objectIds").include("updateSummary.errors")
				.include("deleteSummary.objectIds").include("deleteSummary.errors").include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBodyBulkReturnMessage fromJsonToSSAPBodyBulkReturnMessage(
			String json) {
		return new JSONDeserializer<SSAPBodyBulkReturnMessage>().use(null,
				SSAPBodyBulkReturnMessage.class).deserialize(json);
	}

	public static String toJsonArray(
			Collection<SSAPBodyBulkReturnMessage> collection) {
		return new JSONSerializer().include("insertSummary.objectIds").include("insertSummary.errors")
				.include("updateSummary.objectIds").include("updateSummary.errors")
				.include("deleteSummary.objectIds").include("deleteSummary.errors").exclude("*.class").serialize(collection);
	}

	public static String toJsonArray(
			Collection<SSAPBodyBulkReturnMessage> collection, String[] fields) {
		return new JSONSerializer().include("insertSummary.objectIds").include("insertSummary.errors")
				.include("updateSummary.objectIds").include("updateSummary.errors")
				.include("deleteSummary.objectIds").include("deleteSummary.errors").include(fields).exclude("*.class")
				.serialize(collection);
	}

	public static Collection<SSAPBodyBulkReturnMessage> fromJsonArrayToSSAPBodyBulkReturnMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyBulkReturnMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyBulkReturnMessage.class)
				.deserialize(json);
	}
	
	
	

}
