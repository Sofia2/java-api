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
package com.indra.sofia2.ssap.ssap.body.config.message;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.kp.exceptions.SSAPMessageDeserializationError;

public class SSAPBodyConfigAppsw {
	private String identificacion;
	private Integer versionsw;
	private String url;
	private Integer versioncfg;
	private Map<String, String> propiedadescfg;

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

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toJsonArray(Collection<SSAPBodyConfigAppsw> collection) {
		try {
			return new ObjectMapper().writeValueAsString(collection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPBodyConfigAppsw fromJsonToSSAPBodyConfigAppsw(String json) {
		try {
			return new ObjectMapper().readValue(json, SSAPBodyConfigAppsw.class);
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}

	public static Collection<SSAPBodyConfigAppsw> fromJsonArrayToSSAPBodyConfigAppsws(String json) {
		try {
			return new ObjectMapper().readValue(json, new TypeReference<Collection<SSAPBodyConfigAppsw>>() {});
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}
}
