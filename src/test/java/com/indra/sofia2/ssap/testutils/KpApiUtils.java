package com.indra.sofia2.ssap.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		//log.info(LogUtils.LOG_REQUEST_DATA + joinMessage.toJson());
		SSAPMessage response = kp.send(joinMessage);
		//log.info(LogUtils.LOG_RESPONSE_DATA + Response.toJson());
		assertNotSame(response.getSessionKey(), null);
		sessionKey = response.getSessionKey();
		SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBodyAsJson().toString());
		assertEquals(bodyReturn.getData(), sessionKey);
		assertTrue(bodyReturn.isOk());
		assertSame(bodyReturn.getError(), null);
		return sessionKey;
	}

	public  void doLeave(Kp kp, String sessionKey) throws Exception {
		SSAPMessage leaveMessage = SSAPMessageGenerator.getInstance().generateLeaveMessage(sessionKey);
		//log.info(LogUtils.LOG_REQUEST_DATA + leaveMessage.toJson());
		SSAPMessage response = kp.send(leaveMessage);
		//log.info(LogUtils.LOG_RESPONSE_DATA + response.toJson());
		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBodyAsJson().toString());
		assertEquals(responseBody.getData(), sessionKey);
		assertTrue(responseBody.isOk());
		assertSame(responseBody.getError(), null);
	}
}
