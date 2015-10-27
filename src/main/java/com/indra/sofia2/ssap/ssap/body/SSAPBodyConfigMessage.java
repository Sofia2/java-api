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
package com.indra.sofia2.ssap.ssap.body;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.indra.sofia2.ssap.ssap.body.config.message.SSAPBodyConfigAppsw;
import com.indra.sofia2.ssap.ssap.body.config.message.SSAPBodyConfigAsset;
import com.indra.sofia2.ssap.ssap.body.config.message.SSAPBodyConfigOntologia;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBodyConfigMessage extends SSAPBodyMessage {

	private String kp;

	private String instanciaKp;

	private String token;
	
	private String assetService;
	
	private HashMap<String, String> assetServiceParam;

	private List<SSAPBodyConfigAppsw> lappsw;

	private List<SSAPBodyConfigAsset> lasset;

	private List<SSAPBodyConfigOntologia> lontologia;

	private List<Serializable> lmisc;

	public String getKp() {
		return kp;
	}

	public void setKp(String kp) {
		this.kp = kp;
	}

	public String getInstanciaKp() {
		return instanciaKp;
	}

	public void setInstanciaKp(String instanciaKp) {
		this.instanciaKp = instanciaKp;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAssetService() {
		return assetService;
	}
	
	public void setAssetService(String assetService) {
		this.assetService = assetService;
	}

	public HashMap<String, String> getAssetServiceParam() {
		return assetServiceParam;
	}
	
	public void setAssetServiceParam(HashMap<String, String> assetServiceParam) {
		this.assetServiceParam = assetServiceParam;
	}
	
	public List<SSAPBodyConfigAppsw> getLappsw() {
		return lappsw;
	}

	public void setLappsw(List<SSAPBodyConfigAppsw> lappsw) {
		this.lappsw = lappsw;
	}

	public List<SSAPBodyConfigAsset> getLasset() {
		return lasset;
	}

	public void setLasset(List<SSAPBodyConfigAsset> lasset) {
		this.lasset = lasset;
	}

	public List<SSAPBodyConfigOntologia> getLontologia() {
		return lontologia;
	}

	public void setLontologia(List<SSAPBodyConfigOntologia> lontologia) {
		this.lontologia = lontologia;
	}

	public List<Serializable> getLmisc() {
		return lmisc;
	}

	public void setLmisc(List<Serializable> lmisc) {
		this.lmisc = lmisc;
	}

	public String toJson() {
		return new JSONSerializer().include("lappsw").include("lasset")
				.include("lontologia").include("lmisc").exclude("*.class")
				.serialize(this);
	}

	public static SSAPBodyConfigMessage fromJsonToSSAPBodyConfigMessage(
			String json) {
		return new JSONDeserializer<SSAPBodyConfigMessage>()
				.use(null,SSAPBodyConfigMessage.class).deserialize(json);
	}

	public static Collection<SSAPBodyConfigMessage> fromJsonArrayToSSAPBodyConfigMessages(
			String json) {
		return new JSONDeserializer<List<SSAPBodyConfigMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBodyConfigMessage.class).deserialize(json);
	}

}
