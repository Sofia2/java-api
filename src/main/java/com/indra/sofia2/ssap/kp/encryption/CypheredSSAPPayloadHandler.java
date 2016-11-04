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
