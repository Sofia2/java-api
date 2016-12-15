package com.indra.sofia2.ssap.kp.implementations.oficials.abstracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.sofia2.ssap.kp.KpToExtendApi;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.SSAPMessageGenerator;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSIBException;
import com.indra.sofia2.ssap.kp.exceptions.SSAPResponseTimeoutException;
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
	
	private final static String ONTOLOGY_UPDATE_WHERE = "{Sensor.assetId:\"S_Temperatura_00067\"}";
	private final static String ONTOLOGY_QUERY_NATIVE = "{Sensor.assetId:\"S_Temperatura_00066\"}";
	
	private final static String ONTOLOGY_UPDATE_SQLLIKE = "update TestSensorTemperatura set measure = 20 where Sensor.assetId = \"S_Temperatura_00067\"";
	
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
		SSAPMessage response=kp.send(msgJoin);
				
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, response.toJson()));
		
		assertNotSame(response.getSessionKey(), null);
		
		String sessionKey=response.getSessionKey();
		
		SSAPBodyReturnMessage bodyReturn=SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBodyAsJson().toString());
		assertEquals(bodyReturn.getData(), sessionKey);
		assertTrue(bodyReturn.isOk());
		assertSame(bodyReturn.getError(), null);
		
	}
	
	@Test
	public void testInsertNative() throws Exception {
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgInsert.toJson()));
		SSAPMessage responseInsert=kp.send(msgInsert);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseInsert.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
			
	}
	
	@Test
	public void testUpdateNative() throws Exception {
	
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSTANCE.toString());
		SSAPMessage responseInsert=kp.send(msgInsert);
				
		SSAPMessage msgUpate=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_UPDATE.toString(), ONTOLOGY_UPDATE_WHERE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgUpate.toJson()));
		SSAPMessage responseUpdate=kp.send(msgUpate);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUpdate.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		
	}
	
	@Test
	public void testQueryNative() throws Exception {
		
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE, SSAPQueryType.NATIVE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		
		SSAPMessage responseQuery=kp.send(msgQuery);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		
	}
	
	@Test
	public void testInsertSqlLike() throws Exception {
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, SSAPQueryType.SQLLIKE);
		SSAPBodyOperationMessage messageRequest = SSAPBodyOperationMessage.fromJsonToSSAPBodyOperationMessage(msgInsert.getBody());
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgInsert.toJson()));
		SSAPMessage responseInsert=kp.send(msgInsert);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseInsert.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));		
	}
	
	@Test
	public void testUpdateSqlLike() throws Exception {
		
		SSAPMessage msgUpate=SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME, null, ONTOLOGY_UPDATE_SQLLIKE, SSAPQueryType.SQLLIKE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgUpate.toJson()));
		SSAPMessage responseUpdate=kp.send(msgUpate);

		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUpdate.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void testQuerySql() throws Exception {
	
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME, "select * from " + ONTOLOGY_NAME , SSAPQueryType.SQLLIKE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage responseQuery=kp.send(msgQuery);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseQuery.toJson()));
	}
	
	@Test
	public void testQuerySqlBDC() throws Exception {

		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, null, "select * from Asset where identificacion='tweets_sofia'", SSAPQueryType.BDC);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage responseQuery=kp.send(msgQuery);
		
		
		log.info(responseQuery.toJson());
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBodyAsJson().toString());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void testQueryBDC() throws Exception {
	
		SSAPMessage msgQuery=SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, null, "select * from Asset", SSAPQueryType.BDC);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgQuery.toJson()));
		SSAPMessage responseQuery=kp.send(msgQuery);
	
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseQuery.getBodyAsJson().toString());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		
		assertTrue(returned.isOk());
	}
	
	
	@Test
	public void testSubscribeUnsubscribe() throws Exception{
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String messageId, SSAPMessage ssapMessage) {
				
				log.info(String.format(LogMessages.LOG_NOTIFICATION, messageId, ssapMessage.toJson()));
				
				indicationReceived=true;
			
				SSAPBodyReturnMessage indicationMessage=SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBodyAsJson().toString());
				
				assertNotSame(indicationMessage.getData(), null);
				assertTrue(indicationMessage.isOk());
				assertSame(indicationMessage.getError(), null);
				
			}
		});
				
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey, ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		
		SSAPMessage msgSubscribe = kp.send(msg);
		
		SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBodyAsJson().toString());
		
		assertNotSame(responseSubscribeBody.getData(), null);
		assertTrue(responseSubscribeBody.isOk());
		assertSame(responseSubscribeBody.getError(), null);
		
		String subscriptionId=responseSubscribeBody.getData();
		
		SSAPMessage msgInsert=SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, SSAPQueryType.SQLLIKE);
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgInsert.toJson()));
		
		SSAPMessage responseInsert=kp.send(msgInsert);
		
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseInsert.getBodyAsJson().toString());
		assertTrue(returned.isOk());
				
		Thread.sleep(5000);
		assertTrue(indicationReceived);
		
		SSAPMessage msgUnsubscribe=SSAPMessageGenerator.getInstance().generateUnsubscribeMessage(sessionKey, ONTOLOGY_NAME, subscriptionId);
		
		SSAPMessage responseUnsubscribe=kp.send(msgUnsubscribe);
		SSAPBodyReturnMessage responseUnSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseUnsubscribe.getBodyAsJson().toString());
		 
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
		
		SSAPBulkMessage bulkMessage = SSAPMessageGenerator.getInstance().generateBulkMessage(sessionKey);
		
		bulkMessage.addMessage(msgInsert1);
		bulkMessage.addMessage(msgInsert2);
		bulkMessage.addMessage(msgInsert3);
		bulkMessage.addMessage(msgUpate1);
		bulkMessage.addMessage(msgUpate2);
		String jValue = bulkMessage.toJson();
		
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, bulkMessage.toJson()));
		SSAPMessage respuesta=kp.send(bulkMessage);
		
		SSAPBodyReturnMessage bodyBulkReturn=SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(respuesta.getBodyAsJson().toString());
		SSAPBodyBulkReturnMessage summary=SSAPBodyBulkReturnMessage.fromJsonToSSAPBodyBulkReturnMessage(bodyBulkReturn.getData());
		
		assertEquals(3, summary.getInsertSummary().getObjectIds().size());
		
		StringBuilder sb = new StringBuilder();
		for(String oid:summary.getInsertSummary().getObjectIds()) 
			sb.append(oid);
		
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, sb));
	}
	
	@Test
	public void testLog() throws Exception {		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		SSAPMessage msgLog=SSAPMessageGenerator.getInstance().generateLogMessage(KP, KP_INSTANCE, TOKEN, SSAPLogLevel.INFO, "1", "Mensaje de info enviado", timeStamp);
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgLog.toJson()));
		
		SSAPMessage responseLog=kp.send(msgLog);
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseLog.getBody());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void testError() throws Exception{		
		String timeStamp = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").format(new Date());
		
		SSAPMessage msgError=SSAPMessageGenerator.getInstance().generateErrorMessage(KP, KP_INSTANCE, TOKEN, SSAPSeverityLevel.ERROR, "1", "Mensaje de error enviado", timeStamp);

		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgError.toJson()));
		
		SSAPMessage responseError=kp.send(msgError);
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseError.getBody());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
	}
	
	@Test
	public void testStatus() {		

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		Map<String,String> status = new HashMap<String, String>();
		status.put("atributo1", "valor1");
		status.put("atributo2", "valor2");
		
		SSAPMessage msgStatus = SSAPMessageGenerator.getInstance().generateStatusMessage(KP, KP_INSTANCE, TOKEN, status, timeStamp);

		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgStatus.toJson()));
		
		SSAPMessage responseStatus;
		try {
			responseStatus = kp.send(msgStatus);
			SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseStatus.getBody());
			assertTrue(returned.isOk());
			log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		} catch (ConnectionToSIBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SSAPResponseTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testLocation() throws Exception {		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		SSAPMessage msgLocation=SSAPMessageGenerator.getInstance().generateLocationMessage(KP, KP_INSTANCE, TOKEN, Double.parseDouble("90"), Double.parseDouble("10"), Double.parseDouble("4.5"), Double.parseDouble("0.0"), Double.parseDouble("90.0"), Double.parseDouble("10"), timeStamp);

		log.info(String.format(LogMessages.LOG_REQUEST_DATA, msgLocation.toJson()));
		
		SSAPMessage responseLocation=kp.send(msgLocation);
		
		SSAPMessage replica = SSAPMessage.fromJsonToSSAPMessage(responseLocation.toJson());
		//Checks if location message was OK in SIB
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseLocation.getBody());
		assertTrue(returned.isOk());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));

	}
	
	@Test
	public void testCommand() throws Exception {
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE ,SSAPCommandType.STATUS, Collections.<String, String>emptyMap());
		
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage response = kp.send(request);
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertTrue(returned.isOk());
		assertNotSame(null, returned.getData());
	}
	
	boolean indicationTestSubscribeCommand = false;
	@Test
	public void testSubscribeCommand() throws Exception {
		SSAPMessage request = SSAPMessageGenerator.getInstance().generateSubscribeCommandMessage(KP, KP_INSTANCE, TOKEN, SSAPCommandType.STATUS);
		
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			
			@Override
			public void onIndication(String id, SSAPMessage msg) {
				indicationTestSubscribeCommand = true;
			}
		});
		log.info(String.format(LogMessages.LOG_REQUEST_DATA, request.toJson()));
		SSAPMessage response = kp.send(request);
		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, returned.getData()));
		assertTrue(returned.isOk());
		assertNotSame(null, returned.getData());
		
		SSAPMessage requestCmd = SSAPMessageGenerator.getInstance().generateCommandMessage(sessionKey, KP, KP_INSTANCE ,SSAPCommandType.STATUS, Collections.<String, String>emptyMap());		
		SSAPMessage responseCmd = kp.send(requestCmd);
		
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
			SSAPMessage responseStatus = kp.send(msgStatus);
			log.info(String.format(LogMessages.LOG_RESPONSE_DATA, responseStatus.toJson()));
			
			SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(responseStatus.getBody());
			assertTrue(returned.isOk());
			((KpToExtendApi)this.kp).setStatusReportPeriod(500);
		}
	}
	
	
	

}
