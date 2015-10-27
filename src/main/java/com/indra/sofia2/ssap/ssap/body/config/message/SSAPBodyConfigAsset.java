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

public class SSAPBodyConfigAsset {
	private String identificacion;
	private double latitud;
	private double longitud;
	private Map<String, String> propiedades;
	private Map<String, String> propiedadescfg;
	
	private boolean isNative;
	private String jsonAssets;

	public String toJson() {
		return new JSONSerializer().rootName("asset").include("propiedades")
				.include("propiedadescfg").exclude("*.class").serialize(this);
	}

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
	
	public static SSAPBodyConfigAsset fromJsonToSSAPBodyConfigAsset(String json) {
		return new JSONDeserializer<SSAPBodyConfigAsset>().use(null,
				SSAPBodyConfigAsset.class).deserialize(json);
	}

	public static String toJsonArray(Collection<SSAPBodyConfigAsset> collection) {
		return new JSONSerializer().rootName("asset").exclude("*.class")
				.serialize(collection);
	}

	public static String toJsonArray(
			Collection<SSAPBodyConfigAsset> collection, String[] fields) {
		return new JSONSerializer().rootName("asset").include(fields)
				.exclude("*.class").serialize(collection);
	}

	public static Collection<SSAPBodyConfigAsset> fromJsonArrayToSSAPBodyConfigAssets(
			String json) {
		return new JSONDeserializer<List<SSAPBodyConfigAsset>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyConfigAsset.class).deserialize(json);
	}
}
