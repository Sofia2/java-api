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

public class SSAPBodyConfigAsset {
	private String identificacion;
	private double latitud;
	private double longitud;
	private Map<String, String> propiedades;
	private Map<String, String> propiedadescfg;
	
	private boolean isNative;
	private String jsonAssets;

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public Map<String, String> getPropiedades() {
		return propiedades;
	}

	public void setPropiedades(Map<String, String> propiedades) {
		this.propiedades = propiedades;
	}

	public Map<String, String> getPropiedadescfg() {
		return propiedadescfg;
	}

	public void setPropiedadescfg(Map<String, String> propiedadescfg) {
		this.propiedadescfg = propiedadescfg;
	}

	public boolean isNative() {
		return isNative;
	}
	
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public String getJsonAssets() {
		return jsonAssets;
	}

	public void setJsonAssets(String jsonAssets) {
		this.jsonAssets = jsonAssets;
	}
	
	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJsonArray(Collection<SSAPBodyConfigAsset> collection) {
		try {
			return new ObjectMapper().writeValueAsString(collection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPBodyConfigAsset fromJsonToSSAPBodyConfigAsset(String json) {
		try {
			return new ObjectMapper().readValue(json, SSAPBodyConfigAsset.class);
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}

	public static Collection<SSAPBodyConfigAsset> fromJsonArrayToSSAPBodyConfigAsset(String json) {
		try {
			return new ObjectMapper().readValue(json, new TypeReference<Collection<SSAPBodyConfigAsset>>(){});
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}
}
