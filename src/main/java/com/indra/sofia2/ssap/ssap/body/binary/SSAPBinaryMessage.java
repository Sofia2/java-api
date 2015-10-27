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
package com.indra.sofia2.ssap.ssap.body.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.indra.sofia2.ssap.ssap.binary.Base64;
import com.indra.sofia2.ssap.ssap.binary.Encoder;
import com.indra.sofia2.ssap.ssap.binary.Encoding;
import com.indra.sofia2.ssap.ssap.binary.Mime;
import com.indra.sofia2.ssap.ssap.binary.Storage;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBinaryMessage {

	/**
	 * El fichero serializado
	 */
	private String data;
	/**
	 * La metainformación del fichero
	 */
	private SSAPBinaryMediaMessage media;
	
	public String getData() {
		return data;
	}

	public SSAPBinaryMediaMessage getMedia() {
		return media;
	}

	public byte[] getBinaryData() {
		Encoder encoder=null; 
		switch (media.getBinaryEncoding()) {
		case Base64:
			encoder = new Base64();
			break;
		default:
			break;
		}
		return encoder.decode(getData());
	}

	protected SSAPBinaryMessage(){
		
	}
	
	public SSAPBinaryMessage(File data, Storage sotrageArea, Encoding binaryEncoding, Mime mime){
		this.media = new SSAPBinaryMediaMessage(data.getName(), sotrageArea, binaryEncoding, mime.getValue());
		Encoder encoder=null; 
		switch (binaryEncoding) {
		case Base64:
			encoder = new Base64();
			break;
		default:
			break;
		}
		try {
			this.data=encoder.encode(IOUtils.toByteArray(new FileInputStream(data)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SSAPBinaryMessage fromJsonToSSAPBinaryMessage(String json) {
		return new JSONDeserializer<SSAPBinaryMessage>().use(null,
				SSAPBinaryMessage.class).deserialize(json);
	}

	public static String toJsonArray(Collection<SSAPBinaryMessage> collection) {
		return new JSONSerializer().exclude("*.class").serialize(collection);
	}

	public static String toJsonArray(Collection<SSAPBinaryMessage> collection,
			String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(collection);
	}

	public static Collection<SSAPBinaryMessage> fromJsonArrayToSSAPBinaryMessage(
			String json) {
		return new JSONDeserializer<List<SSAPBinaryMessage>>()
				.use(null, ArrayList.class)
				.use("values", SSAPBinaryMessage.class).deserialize(json);
	}
	
}