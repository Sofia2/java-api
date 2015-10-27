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
import java.util.Map;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodyConfigAppsw {
	private String identificacion;
	private Integer versionsw;
	private String url;
	private Integer versioncfg;
	private Map<String, String> propiedadescfg;

	public String toJson() {
		return new JSONSerializer().rootName("appsw").include("propiedadescfg")
				.exclude("*.class").serialize(this);
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public Integer getVersionsw() {
		return versionsw;
	}

	public void setVersionsw(Integer versionsw) {
		this.versionsw = versionsw;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getVersioncfg() {
		return versioncfg;
	}

	public void setVersioncfg(Integer versioncfg) {
		this.versioncfg = versioncfg;
	}

	public Map<String, String> getPropiedadescfg() {
		return propiedadescfg;
	}

	public void setPropiedadescfg(Map<String, String> propiedadescfg) {
		this.propiedadescfg = propiedadescfg;
	}

	public static SSAPBodyConfigAppsw fromJsonToSSAPBodyConfigAppsw(String json) {
		return new JSONDeserializer<SSAPBodyConfigAppsw>().use(null,
				SSAPBodyConfigAppsw.class).deserialize(json);
	}

	public static String toJsonArray(Collection<SSAPBodyConfigAppsw> collection) {
		return new JSONSerializer().rootName("appsw").exclude("*.class")
				.serialize(collection);
	}

	public static String toJsonArray(
			Collection<SSAPBodyConfigAppsw> collection, String[] fields) {
		return new JSONSerializer().rootName("appsw").include(fields)
				.exclude("*.class").serialize(collection);
	}

	public static Collection<SSAPBodyConfigAppsw> fromJsonArrayToSSAPBodyConfigAppsws(
			String json) {
		return new JSONDeserializer<List<SSAPBodyConfigAppsw>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyConfigAppsw.class).deserialize(json);
	}

}
