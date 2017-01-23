package com.indra.sofia2.ssap.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.SSAPMessageGenerator;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class KpApiUtils {
	
	private static Logger log;
	
	public <T> KpApiUtils(Class<T> clazz) {
		this.log = LoggerFactory.getLogger(clazz);
	}

	public String doJoin(Kp kp, String token, String kp_instance) throws Exception {
		String sessionKey = null;
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(token, kp_instance);
		SSAPMessage response = kp.send(joinMessage);
		assertNotSame(response.getSessionKey(), null);
		sessionKey = response.getSessionKey();
		String kk= joinMessage.toJson();
		SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		
		assertTrue(bodyReturn.isOk());
		assertSame(bodyReturn.getError(), null);
		bodyReturn.getDataAsJsonObject();
		JsonNode jbody = response.getBodyAsJsonObject();
		assertTrue(jbody.path("ok").asBoolean());
		assertTrue(jbody.path("error").isNull());
		return sessionKey;
	}

	public  void doLeave(Kp kp, String sessionKey) throws Exception {
		SSAPMessage leaveMessage = SSAPMessageGenerator.getInstance().generateLeaveMessage(sessionKey);
		SSAPMessage response = kp.send(leaveMessage);
		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertEquals(responseBody.getData(), sessionKey);
		assertTrue(responseBody.isOk());
		assertSame(responseBody.getError(), null);
		
		JsonNode jbody = response.getBodyAsJsonObject();
		assertEquals(jbody.path("data").asText(), sessionKey);
		assertTrue(jbody.path("ok").asBoolean());
		assertTrue(jbody.path("error").isNull());
		
		
	}
}
