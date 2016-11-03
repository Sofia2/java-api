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
package com.indra.sofia2.ssap.ssap.body.binary;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.ssap.binary.Encoding;
import com.indra.sofia2.ssap.ssap.binary.Storage;

public class SSAPBinaryMediaMessage {

	/**
	 * Nombre del fichero
	 */
	private String name;
	/**
	 * Modo de almacenamiento de los datos binarios
	 */
	private Storage storageArea;
	/**
	 * Encoding utilizado para comprimir el fichero
	 */
	private Encoding binaryEncoding;
	/**
	 * Mime asociado al fichero
	 */
	private String mime;
	
	public String getName() {
		return name;
	}
	public Encoding getBinaryEncoding() {
		return binaryEncoding;
	}
	public String getMime() {
		return mime;
	}
	public Storage getStorageArea() {
		return storageArea;
	}
	protected SSAPBinaryMediaMessage(){
		
	}
	
	public SSAPBinaryMediaMessage(String name, Storage storageArea, Encoding binaryEncoding, String mime){
		this.name=name;
		this.storageArea=storageArea;
		this.binaryEncoding=binaryEncoding;
		this.mime=mime;
	}
	
	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toJsonArray(Collection<SSAPBinaryMediaMessage> collection) {
		try {
			return new ObjectMapper().writeValueAsString(collection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static SSAPBinaryMediaMessage fromJsonToSSAPBinaryMediaMessage(String json) throws IOException {
		return new ObjectMapper().readValue(json, SSAPBinaryMediaMessage.class);
	}

	public static Collection<SSAPBinaryMediaMessage> fromJsonArrayToSSAPBinaryMediaMessage(
			String json) throws IOException {
		return new ObjectMapper().readValue(json, new TypeReference<Collection<SSAPBinaryMediaMessage>>(){});
	}
}
