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
package com.indra.sofia2.ssap.ssap.body.config.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodyConfigOntologia {
	private String identificacion;
	private String esquemajson;
	private String tipopermiso;

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public String getEsquemajson() {
		return esquemajson;
	}

	public void setEsquemajson(String esquemajson) {
		this.esquemajson = esquemajson;
	}

	public String getTipopermiso() {
		return tipopermiso;
	}

	public void setTipopermiso(String tipopermiso) {
		this.tipopermiso = tipopermiso;
	}

	public String toJson() {
		return new JSONSerializer().rootName("ontologia").exclude("*.class")
				.serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().rootName("ontologia").include(fields)
				.exclude("*.class").serialize(this);
	}

	public static SSAPBodyConfigOntologia fromJsonToSSAPBodyConfigOntologia(
			String json) {
		return new JSONDeserializer<SSAPBodyConfigOntologia>().use(null,
				SSAPBodyConfigOntologia.class).deserialize(json);
	}

	public static String toJsonArray(
			Collection<SSAPBodyConfigOntologia> collection) {
		return new JSONSerializer().rootName("ontologia").exclude("*.class")
				.serialize(collection);
	}

	public static String toJsonArray(
			Collection<SSAPBodyConfigOntologia> collection, String[] fields) {
		return new JSONSerializer().rootName("ontologia").include(fields)
				.exclude("*.class").serialize(collection);
	}

	public static Collection<SSAPBodyConfigOntologia> fromJsonArrayToSSAPBodyConfigOntologias(
			String json) {
		return new JSONDeserializer<List<SSAPBodyConfigOntologia>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyConfigOntologia.class).deserialize(json);
	}
}
