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
package com.indra.sofia2.ssap.kp.encryption;

import org.apache.commons.codec.binary.Base64;

import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinUserAndPasswordMessage;

public class CypheredSSAPPayloadHandler {
	
	private String xxteaCipherKey;
	
	public CypheredSSAPPayloadHandler(String xxteaCipherKey) {
		this.xxteaCipherKey = xxteaCipherKey;
	}
	
	public byte[] getEncryptedPayload(SSAPMessage msg) {
		byte[] encrypted = XXTEA.encrypt(msg.toJson().getBytes(), this.xxteaCipherKey.getBytes());
		if (msg.getMessageType() == SSAPMessageTypes.JOIN) {
			SSAPBodyJoinUserAndPasswordMessage body = SSAPBodyJoinUserAndPasswordMessage
					.fromJsonToSSAPBodyJoinUserAndPasswordMessage(msg.getBody());
			String kpName = body.getInstance().split(":")[0];
			String completeMessage = kpName.length() + "#" + kpName + Base64.encodeBase64String(encrypted);
			return completeMessage.getBytes();
		} else {
			return Base64.encodeBase64(encrypted);
		}
	}
	
	public String getDecryptedPayload(String payload) {
		if (payload.startsWith("{") && payload.endsWith("}") && payload.contains("direction") && payload
				.contains("sessionKey")) { /* non XXTEA-cyphered message */
			return payload;
		} else {
			byte[] bCifradoBaseado = Base64.decodeBase64(payload);

			for (int i = 0; i < bCifradoBaseado.length; i++) {
				bCifradoBaseado[i] = (byte) (bCifradoBaseado[i] & 0xFF);
			}

			String clearMessage = new String(
					XXTEA.decrypt(bCifradoBaseado, xxteaCipherKey.getBytes()));
			return clearMessage;
		}
	}
}
