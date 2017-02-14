package com.indra.sofia2.ssap.kp.implementations.oficials.abstracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.sofia2.ssap.kp.KpToExtendApi;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.SSAPMessageGenerator;
import com.indra.sofia2.ssap.kp.logging.LogMessages;
import com.indra.sofia2.ssap.ssap.SSAPCommandType;
import com.indra.sofia2.ssap.ssap.SSAPErrorCode;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageDirection;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.SSAPQueryType;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;
import com.indra.sofia2.ssap.ssap.exceptions.SQLSentenceNotAllowedForThisOperationException;
import com.indra.sofia2.ssap.testutils.FixtureLoader;
import com.indra.sofia2.ssap.testutils.KpApiUtils;
import com.indra.sofia2.ssap.testutils.TestProperties;

public abstract class KpErrorsAbstract {
	
	protected static Logger log;
	protected static KpApiUtils utils;
	
	protected final static String TOKEN = TestProperties.getInstance().get("test.officials.token");
	protected final static String KP = TestProperties.getInstance().get("test.officials.kp");
	protected final static String KP_INSTANCE = TestProperties.getInstance().get("test.officials.kp_instance");
	
	private final static String ONTOLOGY_NAME = TestProperties.getInstance().get("test.officials.ontology_name");
	
	private static JsonNode ONTOLOGY_INSTANCE;
	private static JsonNode COMMAND_REQ_INSTANCE;
	private static JsonNode ONTOLOGY_UPDATE;
	private static JsonNode ONTOLOGY_QUERY_NATIVE_CRITERIA;
	private static JsonNode ONTOLOGY_DELETE;
	
	private final static String ONTOLOGY_QUERY_NATIVE_STATEMENT = "db.TestSensorTemperatura.find({\"Sensor.assetId\": \"S_Temperatura_00066\"})";
	private final static String ONTOLOGY_QUERY_SQLLIKE = "select * from TestSensorTemperatura where Sensor.assetId = \"S_Temperatura_00066\"";
	private final static String ONTOLOGY_INSERT_SQLLIKE = "insert into TestSensorTemperatura(geometry, assetId, measure, timestamp) values (\"{ 'coordinates': [ 40.512967, -3.67495 ], 'type': 'Point' }\", \"S_Temperatura_00066\", 15, \"{ '$date': '2014-04-29T08:24:54.005Z'}\")";
	
	private final static String ONTOLOGY_UPDATE_WHERE = "{Sensor.assetId:\"S_Temperatura_00066\"}";
	private final static String ONTOLOGY_QUERY_NATIVE = "{Sensor.measure:{$gt:10}}";
	private final static String MALFORMED_ONTOLOGY_QUERY_NATIVE = "{Sensor.measure:{$gtXX:10}}";
	
	private final static String ONTOLOGY_UPDATE_SQLLIKE = "update TestSensorTemperatura set measure = 20 where Sensor.assetId = \"S_Temperatura_00066\"";
	private final static String MALFORMED_ONTOLOGY_UPDATE_SQLLIKE = "update TestSensorTemperatura set measure = 20 where SensorXX.assetId = \"S_Temperatura_00066\"";
	
	private static KpToExtendApi kp;
	
	private boolean indicationReceived;
	private String sessionKey;
	
	public abstract KpToExtendApi getImplementation();
	public abstract Logger getLog();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		FixtureLoader loader = new FixtureLoader();
		
		ONTOLOGY_INSTANCE = loader.load("ontology_instance");
		COMMAND_REQ_INSTANCE = loader.load("command_req_instance");
		ONTOLOGY_UPDATE = loader.load("ontology_update_no_id");
		ONTOLOGY_QUERY_NATIVE_CRITERIA = loader.load("ontology_quey_native_criteria");
		ONTOLOGY_DELETE = loader.load("ontology_delete");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}	

	@Before
	public void setUp() throws Exception {
		log = this.getLog();
		utils = new KpApiUtils(getClass());
		kp= this.getImplementation();
		kp.connect();
		kp.disableStatusReport();
		sessionKey = utils.doJoin(kp, TOKEN, KP_INSTANCE);
	}

	@After
	public void tearDown() throws Exception {
		utils.doLeave(kp, sessionKey);
		kp.disconnect();
	}
	
	

	@Test
	public void renewSessionWithBadToken() throws Exception {
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN + "MALFORMED", KP_INSTANCE);
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(joinMessage);
		
		assertEquals(response.getSessionKey(), null);
		//SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage bodyReturn = response.getBodyAsObject();
		assertEquals(bodyReturn.getData(), null);
		assertFalse(bodyReturn.isOk());
		//assertEquals(SSAPErrorCode.AUTENTICATION, bodyReturn.getErrorCode());
		assertNotEquals(bodyReturn.getError(), null);	
	}
	
	@Test
	public void renewSessionWithInactiveToken() throws Exception
	{
		//TODO: create inative token in sofia2.com
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN + "INACTIVE", KP_INSTANCE);
		joinMessage.setSessionKey(sessionKey);
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(joinMessage);
		
		assertEquals(response.getSessionKey(), null);
		//SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage bodyReturn = response.getBodyAsObject();
		assertEquals(bodyReturn.getData(), null);
		assertFalse(bodyReturn.isOk());
		//assertEquals(SSAPErrorCode.AUTENTICATION, bodyReturn.getErrorCode());
		assertNotEquals(bodyReturn.getError(), null);	
	}
	
	@Test
	public void renewSessionWithBadSessionKey() throws Exception {
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN, KP_INSTANCE);
		joinMessage.setSessionKey(sessionKey + "BAD_SESSION");
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(joinMessage);
		
		assertEquals(response.getSessionKey(), null);
		//SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage bodyReturn = response.getBodyAsObject();
		assertEquals(bodyReturn.getData(), null);
		assertFalse(bodyReturn.isOk());
		//assertEquals(SSAPErrorCode.AUTENTICATION, bodyReturn.getErrorCode());
		assertNotEquals(bodyReturn.getError(), null);
	}
	
	@Test
	public void closeSessionWithBadSessionKey() throws Exception{
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN, KP_INSTANCE);
		joinMessage.setSessionKey(sessionKey + "BAD_SESSION");
		SSAPMessage<SSAPBodyReturnMessage> respJoin = kp.send(joinMessage);
			
		SSAPMessage leaveMessage = SSAPMessageGenerator.getInstance().generateLeaveMessage(respJoin.getSessionKey() + "BAD_SESSION");
		SSAPMessage<SSAPBodyReturnMessage> respLeave = kp.send(leaveMessage);
		//SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(respLeave.getBody());
		SSAPBodyReturnMessage bodyReturn = respLeave.getBodyAsObject();
		assertEquals(bodyReturn.getData(), null);
		//assertEquals(SSAPErrorCode.AUTENTICATION, bodyReturn.getErrorCode());
		assertFalse(bodyReturn.isOk());
		assertNotEquals(bodyReturn.getError(), null);
		
	}
	
	@Test
	public void queryNativeBadSessionKey() throws Exception
	{
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey + "BAD_SESSION", ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> responseQuery=kp.send(msgQuery);
		
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBody());
		SSAPBodyReturnMessage returned = responseQuery.getBodyAsObject();
		assertFalse(returned.isOk());
		//assertEquals(SSAPErrorCode.AUTENTICATION, bodyReturn.getErrorCode());
		assertNotEquals(returned.getError(), null);
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));

	}
	
	@Test
	public void queryNativeMalformedQuery() throws Exception {
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, MALFORMED_ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> responseQuery=kp.send(msgQuery);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBody());
		SSAPBodyReturnMessage returned = responseQuery.getBodyAsObject();
		assertFalse(returned.isOk());
		assertNotEquals(returned.getError(), null);
		assertNotEquals(returned.getErrorCode(), null);
		assertEquals(SSAPErrorCode.OTHER, returned.getErrorCode());
	
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void queryNativeBadQueryType() throws Exception{
		SSAPMessage msgQuery = null;
		try {
			msgQuery = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.SQLLIKE);
		} catch (SQLSentenceNotAllowedForThisOperationException e) {
			assertEquals(true, e.getClass().equals(SQLSentenceNotAllowedForThisOperationException.class));
			return;
		}
	}
	
	@Test
	public void queryNativeBadMessageType() throws Exception{
		
		SSAPMessage msgQuery = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		msgQuery.setMessageType(SSAPMessageTypes.CONFIG);
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> responseQuery=kp.send(msgQuery);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBody());
		SSAPBodyReturnMessage returned = responseQuery.getBodyAsObject();
		assertFalse(returned.isOk());
		assertNotEquals(returned.getError(), null);
		assertNotEquals(returned.getErrorCode(), null);
		//assertEquals(SSAPErrorCode.OTHER, returned.getErrorCode());
	
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void querySqlLikeMalformedQuery() throws Exception {
		SSAPMessage msgQuery = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, MALFORMED_ONTOLOGY_UPDATE_SQLLIKE, SSAPQueryType.SQLLIKE);
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> responseQuery=kp.send(msgQuery);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBody());
		SSAPBodyReturnMessage returned = responseQuery.getBodyAsObject();
		assertFalse(returned.isOk());
		assertNotEquals(returned.getError(), null);
		assertNotEquals(returned.getErrorCode(), null);
		assertEquals(SSAPErrorCode.PERSISTENCE, returned.getErrorCode());
	
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void subscribeBadSessionKey() throws Exception {
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			@Override
			public void onIndication(String idNotifition, SSAPMessage message) {}
		});
		
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey + "BAD_SESSION", ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		SSAPMessage<SSAPBodyReturnMessage> msgSubscribe = kp.send(msg);
		
		//SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBody());
		SSAPBodyReturnMessage responseSubscribeBody = msgSubscribe.getBodyAsObject();
		
		assertSame(responseSubscribeBody.getData(), null);
		assertNotEquals(true, responseSubscribeBody.isOk());
		assertNotEquals(null, responseSubscribeBody.getError());
		assertEquals(SSAPErrorCode.AUTENTICATION, responseSubscribeBody.getErrorCode());
		
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, msgSubscribe.getBody()));
		
	}
	
	@Test
	public void unsubscribeBadSessionKey() throws Exception {
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			@Override
			public void onIndication(String idNotifition, SSAPMessage message) {}
		});
		
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey , ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		SSAPMessage<SSAPBodyReturnMessage> msgSubscribe = kp.send(msg);
		
		SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBody());
		String subscriptionId=responseSubscribeBody.getData();
		
		SSAPMessage msgUnsubscribe=SSAPMessageGenerator.getInstance().generateUnsubscribeMessage(sessionKey + "BAD_SESSION", ONTOLOGY_NAME, subscriptionId);
		
		SSAPMessage<SSAPBodyReturnMessage> responseUnsubscribe=kp.send(msgUnsubscribe);
		//SSAPBodyReturnMessage responseUnSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUnsubscribe.getBody());
		SSAPBodyReturnMessage responseUnSubscribeBody = responseUnsubscribe.getBodyAsObject();
		
		assertNotEquals(true, responseUnSubscribeBody.isOk());
		assertNotEquals(null, responseUnSubscribeBody.getError());
		assertEquals(SSAPErrorCode.AUTENTICATION, responseUnSubscribeBody.getErrorCode());
			
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseUnsubscribe.getBody()));
	}
	
	@Ignore
	@Test
	public void unsubscribeBadSuscriptionId() throws Exception {
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			@Override
			public void onIndication(String idNotifition, SSAPMessage message) {}
		});
		
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey , ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		SSAPMessage<SSAPBodyReturnMessage> msgSubscribe = kp.send(msg);
		
		SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBody());
		String subscriptionId=responseSubscribeBody.getData();
		
		SSAPMessage msgUnsubscribe=SSAPMessageGenerator.getInstance().generateUnsubscribeMessage(sessionKey, ONTOLOGY_NAME, subscriptionId + "BAD_ID");
		
		SSAPMessage<SSAPBodyReturnMessage> responseUnsubscribe=kp.send(msgUnsubscribe);
		//SSAPBodyReturnMessage responseUnSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUnsubscribe.getBody());
		SSAPBodyReturnMessage responseUnSubscribeBody = responseUnsubscribe.getBodyAsObject();
		
		assertNotEquals(true, responseUnSubscribeBody.isOk());
		assertNotEquals(null, responseUnSubscribeBody.getError());
		assertEquals(SSAPErrorCode.AUTENTICATION, responseUnSubscribeBody.getErrorCode());
			
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseUnsubscribe.getBody()));
	}
	
	boolean indicationTestSubscribeCommand = false;
	@Test
	public void testSubscribeCommandKpInstanceNotMatch() throws Exception {
		indicationTestSubscribeCommand= false;
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateSubscribeCommandMessage(KP, KP_INSTANCE, TOKEN, SSAPCommandType.STATUS);
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String id, SSAPMessage msg) {
				indicationTestSubscribeCommand = true;
			}
		});
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(request);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertTrue(returned.isOk());
		assertNotSame(null, returned.getData());
		
		SSAPMessage requestCmd = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE + "NOT_MATCH" ,SSAPCommandType.STATUS, Collections.<String, String>emptyMap());		
		SSAPMessage<SSAPBodyReturnMessage> responseCmd = kp.send(requestCmd);
		
		Thread.sleep(5000);
		
		assertFalse(indicationTestSubscribeCommand);
	}
	
	@Test
	public void testSubscribeCommandTypeNull() throws Exception {
		indicationTestSubscribeCommand= false;
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateSubscribeCommandMessage(KP, KP_INSTANCE, TOKEN, SSAPCommandType.STATUS);
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String id, SSAPMessage msg) {
				indicationTestSubscribeCommand = true;
			}
		});
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(request);
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertTrue(returned.isOk());
		assertNotSame(null, returned.getData());
		
		SSAPMessage requestCmd = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE , null, Collections.<String, String>emptyMap());		
		SSAPMessage<SSAPBodyReturnMessage> responseCmd = kp.send(requestCmd);
		
		Thread.sleep(5000);
		
		assertFalse(indicationTestSubscribeCommand);
	}
	
	@Test 
	@Ignore
	public void testInsertUnknownMessage() throws Exception{
		
		String unknownBody = "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\",\"key4\":\"value4\"}";
		SSAPMessage request = new SSAPMessage();
		request.setDirection(SSAPMessageDirection.REQUEST);
		request.setSessionKey(sessionKey);
		request.setBody(unknownBody);
		
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(request);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertFalse(returned.isOk());
		assertEquals(SSAPErrorCode.OTHER, returned.getErrorCode());
		assertNotSame(null, returned.getError());
		
	}

	
}
