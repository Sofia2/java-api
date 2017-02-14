package com.indra.sofia2.ssap.kp.implementations.oficials.abstracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.sofia2.ssap.json.JSON;
import com.indra.sofia2.ssap.kp.KpToExtendApi;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.SSAPMessageGenerator;
import com.indra.sofia2.ssap.kp.logging.LogMessages;
import com.indra.sofia2.ssap.kp.utils.functions.Supplier;
import com.indra.sofia2.ssap.ssap.SSAPBulkMessage;
import com.indra.sofia2.ssap.ssap.SSAPCommandType;
import com.indra.sofia2.ssap.ssap.SSAPLogLevel;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPQueryType;
import com.indra.sofia2.ssap.ssap.SSAPSeverityLevel;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyOperationMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;
import com.indra.sofia2.ssap.ssap.body.bulk.message.SSAPBodyBulkReturnMessage;
import com.indra.sofia2.ssap.testutils.FixtureLoader;
import com.indra.sofia2.ssap.testutils.KpApiUtils;
import com.indra.sofia2.ssap.testutils.TestProperties;

public abstract class KpFunctionalAbstract {

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
	
	private final static String ONTOLOGY_QUERY_NATIVE_STATEMENT = "db.TestSensorTemperatura.find({'Sensor.assetId': 'S_Temperatura_00066'})";
	private final static String ONTOLOGY_QUERY_SQLLIKE = "select * from TestSensorTemperatura where Sensor.assetId = 'S_Temperatura_00066'";
	private final static String ONTOLOGY_INSERT_SQLLIKE = "insert into TestSensorTemperatura(geometry, assetId, measure, timestamp) values (\"{ 'coordinates': [ 40.512967, -3.67495 ], 'type': 'Point' }\", \"S_Temperatura_00067\", 15, \"{ '$date': '2014-04-29T08:24:54.005Z'}\")";
	
	private final static String ONTOLOGY_UPDATE_WHERE = "{Sensor.assetId:\"S_Temperatura_00066\"}";
	private final static String ONTOLOGY_QUERY_NATIVE = "{Sensor.assetId:\"S_Temperatura_00066\"}";
	
	private final static String ONTOLOGY_UPDATE_SQLLIKE = "update TestSensorTemperatura set measure = 15 where Sensor.assetId = \"S_Temperatura_00067\"";
	
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
	public void testJoinByTokenLeave( ) throws Exception {	
		
		SSAPMessage msgJoin=SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN, KP_INSTANCE);
		SSAPMessage<SSAPBodyReturnMessage> response= kp.send(msgJoin);
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
		assertNotSame(response.getSessionKey(), null);
		
		String sessionKey=response.getSessionKey();
		
		SSAPBodyReturnMessage bodyReturn=response.getBodyAsObject();
		assertEquals(bodyReturn.getData(), sessionKey);
		assertTrue(bodyReturn.isOk());
		assertSame(bodyReturn.getError(), null);
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertEquals(jBody.path("data").asText(), sessionKey);
		assertTrue(jBody.path("ok").asBoolean());
		assertTrue(jBody.path("error").isNull());
		
	}
	
	@Test
	public void testInsertNative() throws Exception {
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgInsert.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(msgInsert);
		
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testUpdateNative() throws Exception {
	
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgInsert);
				
		SSAPMessage msgUpate=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_UPDATE.toString(), ONTOLOGY_UPDATE_WHERE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgUpate.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> responseUpdate=kp.send(msgUpate);
		
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
	}
	
	@Test
	public void testQueryNative() throws Exception {
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgQuery);
				
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(returned.isOk());

		JsonNode jBody = response.getBodyAsJsonObject();
		JsonNode jReturned = returned.getDataAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		Iterator<JsonNode> iterator = jReturned.iterator();
					
		while(iterator.hasNext()) {
			JsonNode item = iterator.next();
			assertTrue(!item.path("_id").isNull());
		}
		
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testInsertSqlLike() throws Exception {
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, SSAPQueryType.SQLLIKE);
		SSAPBodyOperationMessage messageRequest = SSAPBodyOperationMessage.fromJsonToSSAPBodyOperationMessage(msgInsert.getBody());
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgInsert.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgInsert);
		
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		JsonNode jReturned = returned.getDataAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
				
	}
	
	@Test
	public void testUpdateSqlLike() throws Exception {
		
		SSAPMessage msgUpate=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, null, ONTOLOGY_UPDATE_SQLLIKE, SSAPQueryType.SQLLIKE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgUpate.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgUpate);

		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		JsonNode jReturned = returned.getDataAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testQuerySql() throws Exception {
	
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, "select * from " + ONTOLOGY_NAME , SSAPQueryType.SQLLIKE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgQuery);
		
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testQuerySqlBDC() throws Exception {

		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, null, "select * from Asset where identificacion='tweets_sofia'", SSAPQueryType.BDC);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgQuery);
				
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testQueryBDC() throws Exception {
	
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, null, "select * from Asset", SSAPQueryType.BDC);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgQuery);
	
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned=response.getBodyAsObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
		assertTrue(returned.isOk());
	}
	
	@Test
	public void testQueryBDH() throws Exception {
	
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgQuery);
				
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		
		returned.setData("{  \"columns\": [{ \"name\": \"_id\", \"type\": \"VARCHAR\",\"index\": 1},{\"name\": \"contextdata.session_key\",\"type\": \"VARCHAR\", \"index\": 2    }, {\"name\": \"contextdata.user\", \"type\": \"VARCHAR\", \"index\": 3 },{ \"name\": \"contextdata.kp\", \"type\": \"VARCHAR\",\"index\": 4 }, { \"name\": \"contextdata.kp_identificador\",  \"type\": \"VARCHAR\", \"index\": 5}, {\"name\": \"contextdata.timestamp\",  \"type\": \"TIMESTAMP\",  \"index\": 6}, { \"name\": \"feedid\", \"type\": \"VARCHAR\", \"index\": 7}, { \"name\": \"feedsource\", \"type\": \"VARCHAR\",  \"index\": 8 }, { \"name\": \"assetid\",  \"type\": \"VARCHAR\", \"index\": 9 }, { \"name\": \"assettype\", \"type\": \"VARCHAR\",       \"index\": 10    },     {      \"name\": \"assetsource\",       \"type\": \"VARCHAR\",       \"index\": 11    },     {      \"name\": \"assetname\",       \"type\": \"VARCHAR\",       \"index\": 12    },     {      \"name\": \"type\",       \"type\": \"VARCHAR\",       \"index\": 13    },     {      \"name\": \"timestamp\",       \"type\": \"TIMESTAMP\",       \"index\": 14    },     {      \"name\": \"measurestimestamp\",       \"type\": \"TIMESTAMP\",       \"index\": 15    },     {      \"name\": \"measurestimestampend_\",       \"type\": \"TIMESTAMP\",       \"index\": 16    },     {      \"name\": \"measurestype\",       \"type\": \"VARCHAR\",       \"index\": 17    },     {      \"name\": \"measuresperiod\",       \"type\": \"FLOAT\",       \"index\": 18    },     {      \"name\": \"measuresperiodunit\",       \"type\": \"VARCHAR\",       \"index\": 19    }  ],   \"values\": [    [      \"582fa47ee4b0045ff0087d22\",       \"2f1a1f0a-fcd0-4b65-8db4-402926a665d7\",       \"mazomacarra\",       \"kpBiciCoruna\",       \"kpBiciCoruna1\",       \"2016-11-19 01:01:50.39\",       \"feed_5_2016-11-19T00:54:59\",       \"BiciCoruna\",       \"5\",       \"BiciCoruna\",       \"BiciCoruna\",       \"Aquarium\",       \"VIRTUAL\",       \"2016-11-19 00:54:59.206\",       \"2016-11-19 00:54:59.206\",       null,       \"INSTANT\",       60,       \"s\"    ],     [      \"582fa47ee4b0045ff0087d1f\",       \"05b56a9b-70a0-405e-a0cd-ae24db51464d\",       \"mazomacarra\",       \"kpBiciCoruna\",       \"kpBiciCoruna1\",       \"2016-11-19 01:01:50.243\",       \"feed_11_2016-11-19T00:54:59\",       \"BiciCoruna\",       \"11\",       \"BiciCoruna\",       \"BiciCoruna\",       \"Castros\",       \"VIRTUAL\",       \"2016-11-19 00:54:59.281\",       \"2016-11-19 00:54:59.281\",       null,       \"INSTANT\",       60,       \"s\"    ],     [      \"582fa47de4b0045ff0087d19\",       \"05b56a9b-70a0-405e-a0cd-ae24db51464d\", \"mazomacarra\", \"kpBiciCoruna\", \"kpBiciCoruna1\", \"2016-11-19 01:01:49.818\", \"feed_8_2016-11-19T00:54:59\", \"BiciCoruna\", \"8\", \"BiciCoruna\", \"BiciCoruna\", \"Millenium\", \"VIRTUAL\", \"2016-11-19 00:54:59.282\",  \"2016-11-19 00:54:59.282\", null, \"INSTANT\", 60, \"s\"]]}");
		
		JsonNode jNode = returned.getDataAsJsonObject();
		
		assertTrue(returned.isOk());

		JsonNode jBody = response.getBodyAsJsonObject();
		JsonNode jReturned = returned.getDataAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, jReturned.toString()));
		Iterator<JsonNode> iterator = jReturned.iterator();
					
		while(iterator.hasNext()) {
			JsonNode item = iterator.next();
			assertTrue(!item.path("_id").isNull());
		}
	}
	
	
	@Test
	public void testSubscribeUnsubscribe() throws Exception{
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String messageId, SSAPMessage ssapMessage) {
				
				log.info(String.format(LogMessages.LOG_NOTIFICATION, messageId, ssapMessage.toJson()));
				
				indicationReceived=true;
			
				SSAPBodyReturnMessage indicationMessage=SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody());
				
				assertNotSame(indicationMessage.getData(), null);
				assertTrue(indicationMessage.isOk());
				assertSame(indicationMessage.getError(), null);
				log.info(String.format(LogMessages.LOG_RESPONSE_DATA, ssapMessage.toJson()));
				
			}
		});
				
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey, ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		
		SSAPMessage<SSAPBodyReturnMessage> msgSubscribe = kp.send(msg);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgSubscribe.toJson()));
		//SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBody());
		SSAPBodyReturnMessage responseSubscribeBody=msgSubscribe.getBodyAsObject();
		assertNotSame(responseSubscribeBody.getData(), null);
		assertTrue(responseSubscribeBody.isOk());
		assertSame(responseSubscribeBody.getError(), null);
		
		String subscriptionId=responseSubscribeBody.getData();
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, SSAPQueryType.SQLLIKE);
		
		
		SSAPMessage<SSAPBodyReturnMessage> responseInsert=kp.send(msgInsert);
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseInsert.toJson()));
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseInsert.getBody());
		assertTrue(returned.isOk());
				
		Thread.sleep(5000);
		assertTrue(indicationReceived);
		
		SSAPMessage msgUnsubscribe=SSAPMessageGenerator.getInstance().generateUnsubscribeMessage(sessionKey, ONTOLOGY_NAME, subscriptionId);
		
		SSAPMessage<SSAPBodyReturnMessage> responseUnsubscribe=kp.send(msgUnsubscribe);
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseUnsubscribe.toJson()));
		//SSAPBodyReturnMessage responseUnSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUnsubscribe.getBody());
		SSAPBodyReturnMessage responseUnSubscribeBody=responseUnsubscribe.getBodyAsObject();
		 
		assertEquals(responseUnSubscribeBody.getData(), "");
		assertTrue(responseUnSubscribeBody.isOk());
		assertSame(responseUnSubscribeBody.getError(), null);
	
	}
	
	@Test
	public void testBulk() throws Exception {
		
		SSAPMessage msgInsert1=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		SSAPMessage msgInsert2=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		SSAPMessage msgInsert3=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, SSAPQueryType.SQLLIKE);
		SSAPMessage msgUpate1=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_UPDATE.toString(), ONTOLOGY_UPDATE_WHERE);
		SSAPMessage msgUpate2=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, null, ONTOLOGY_UPDATE_SQLLIKE, SSAPQueryType.SQLLIKE);
		
		SSAPBulkMessage request = SSAPMessageGenerator.getInstance().generateBulkMessage(sessionKey);
		
		request.addMessage(msgInsert1);
		request.addMessage(msgInsert2);
		request.addMessage(msgInsert3);
		request.addMessage(msgUpate1);
		request.addMessage(msgUpate2);
			
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(request);
		JsonNode jBody = response.getBodyAsJsonObject();
		if(jBody.isArray()) {
			 for (final JsonNode objNode : jBody) {
			        System.out.println(objNode.toString());
			    }
		}
		
		SSAPBodyReturnMessage bodyBulkReturn=SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		System.out.println(bodyBulkReturn.getData());
		SSAPBodyBulkReturnMessage summary=SSAPBodyBulkReturnMessage.fromJsonToSSAPBodyBulkReturnMessage(bodyBulkReturn.getData());
		
		assertEquals(3, summary.getInsertSummary().getObjectIds().size());
		System.out.println(summary.getUpdateSummary().getObjectIds().size());
		StringBuilder sb = new StringBuilder();
		for(String oid:summary.getInsertSummary().getObjectIds()) 
			sb.append(oid);
		
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testLog() throws Exception {		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		SSAPMessage msgLog=SSAPMessageGenerator.getInstance().generateLogMessage(KP, KP_INSTANCE, TOKEN, SSAPLogLevel.INFO, "1", "Mensaje de info enviado", timeStamp);
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgLog.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgLog);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testError() throws Exception{		
		String timeStamp = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").format(new Date());
		
		SSAPMessage msgError=SSAPMessageGenerator.getInstance().generateErrorMessage(KP, KP_INSTANCE, TOKEN, SSAPSeverityLevel.ERROR, "1", "Mensaje de error enviado", timeStamp);
		SSAPMessage msgLog=SSAPMessageGenerator.getInstance().generateLogMessage(KP, KP_INSTANCE, TOKEN, SSAPLogLevel.INFO, "1", "Mensaje de info enviado", timeStamp);
		
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgError.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgError);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
	}
	
	@Test
	public void testStatus() throws Exception {		

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		Map<String,String> status = new HashMap<String, String>();
		status.put("atributo1", "valor1");
		status.put("atributo2", "valor2");
		
		SSAPMessage msgStatus = SSAPMessageGenerator.getInstance().generateStatusMessage(KP, KP_INSTANCE, TOKEN, status, timeStamp);

		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgStatus.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(msgStatus);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
		
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
	}
	
	@Test
	public void testLocation() throws Exception {		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		SSAPMessage msgLocation=SSAPMessageGenerator.getInstance().generateLocationMessage(KP, KP_INSTANCE, TOKEN, Double.parseDouble("90"), Double.parseDouble("10"), Double.parseDouble("4.5"), Double.parseDouble("0.0"), Double.parseDouble("90.0"), Double.parseDouble("10"), timeStamp);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgLocation.toJson()));
		
		SSAPMessage<SSAPBodyReturnMessage> response=kp.send(msgLocation);
		
		//Checks if location message was OK in SIB
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		assertTrue(returned.isOk());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
				
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));

	}
	
	@Test
	public void testCommand() throws Exception {
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE ,SSAPCommandType.STATUS, Collections.<String, String>emptyMap());
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(request);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertTrue(returned.isOk());
		
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
				
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
		assertNotSame(null, returned.getData());
		assertTrue(!jReturned.isNull());
		
	}
	
	boolean indicationTestSubscribeCommand = false;
	@Test
	public void testSubscribeCommand() throws Exception {
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateSubscribeCommandMessage(KP, KP_INSTANCE, TOKEN, SSAPCommandType.STATUS);
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String id, SSAPMessage msg) {
				indicationTestSubscribeCommand = true;
				log.info(String.format(LogMessages.LOG_RESPONSE_DATA, msg.toJson()));
			}
		});
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage<SSAPBodyReturnMessage> response = kp.send(request);
		//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyReturnMessage returned = response.getBodyAsObject();
		assertTrue(returned.isOk());
		assertNotSame(null, returned.getData());
		
		JsonNode jBody = response.getBodyAsJsonObject();
		assertTrue(jBody.path("ok").asBoolean());
				
		JsonNode jReturned = returned.getDataAsJsonObject();
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
		
		SSAPMessage requestCmd = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE ,SSAPCommandType.STATUS, Collections.<String, String>emptyMap());		
		SSAPMessage<SSAPBodyReturnMessage> responseCmd = kp.send(requestCmd);
		
		Thread.sleep(5000);
		
		assertTrue(indicationTestSubscribeCommand);
	}
	
	
	@Test
	public void testWithStatusReporterInBackground() throws Exception{	
		
		this.kp.enableStatusReport();
        this.kp.setStatusReportPeriod(500);
		this.kp.setCustomStatusSupplier(new Supplier<Map<String,String>>() {		
			@Override
			public Map<String, String> get() {
				Map<String,String> status = new HashMap<String, String>();
				status.put("custom_foo1", "custom_bar1");
				status.put("custom_foo2", "custom_bar2");
				return status;
			}
		});
		
		for(int i = 0; i < 20; i++) {
			Thread.sleep(500);
			String timeStamp = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").format(new Date());
			
			Map<String,String> status = new HashMap<String, String>();
			status.put("foo1", "bar1");
			status.put("foo2", "bar2");
			SSAPMessage msgStatus=SSAPMessageGenerator.getInstance().generateStatusMessage(KP, KP_INSTANCE, TOKEN, status, timeStamp);
			
			log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgStatus.toJson()));
			SSAPMessage<SSAPBodyReturnMessage> responseStatus = kp.send(msgStatus);
			log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseStatus.toJson()));
			
			//SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseStatus.getBody());
			SSAPBodyReturnMessage returned = responseStatus.getBodyAsObject();
			assertTrue(returned.isOk());
			
			JsonNode jBody = responseStatus.getBodyAsJsonObject();
			JsonNode jReturned = returned.getDataAsJsonObject();
						
			((KpToExtendApi)this.kp).setStatusReportPeriod(500);
		}
		
	}
	
	
	

}
