package com.indra.sofia2.ssap.kp.implementations.oficials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.indra.sofia2.ssap.json.JSON;
import com.indra.sofia2.ssap.kp.implementations.rest.SSAPResourceAPI;
import com.indra.sofia2.ssap.kp.implementations.rest.resource.SSAPResource;
import com.indra.sofia2.ssap.kp.logging.LogMessages;
import com.indra.sofia2.ssap.ssap.SSAPCommandType;
import com.indra.sofia2.ssap.ssap.SSAPLogLevel;
import com.indra.sofia2.ssap.ssap.SSAPSeverityLevel;
import com.indra.sofia2.ssap.testutils.FixtureLoader;
import com.indra.sofia2.ssap.testutils.LightHttpListener;
import com.indra.sofia2.ssap.testutils.RestApiUtils;
import com.indra.sofia2.ssap.testutils.TestProperties;

import javax.ws.rs.core.Response;

public class TestRestApiFunctional {

	private static Logger log =  LoggerFactory.getLogger(TestRestApiFunctional.class);
	private static RestApiUtils utils = new RestApiUtils(TestRestApiFunctional.class);

	private final static String TOKEN = TestProperties.getInstance().get("test.officials.token");
	private final static String KP = TestProperties.getInstance().get("test.officials.kp");
	private final static String KP_INSTANCE = TestProperties.getInstance().get("test.officials.kp_instance");
	
	private final static String ONTOLOGY_NAME = TestProperties.getInstance().get("test.officials.ontology_name");
	
	private final static String SERVICE_URL=TestProperties.getInstance().get("test.officials.rest.url");

	
	private static JsonNode ONTOLOGY_INSTANCE;
	private static JsonNode COMMAND_REQ_INSTANCE;
	private static JsonNode ONTOLOGY_UPDATE;
	private static JsonNode ONTOLOGY_QUERY_NATIVE_CRITERIA;
	private static JsonNode ONTOLOGY_DELETE;
	
	private final static String ONTOLOGY_QUERY_NATIVE_STATEMENT = "db.TestSensorTemperatura.find({\"Sensor.assetId\": \"S_Temperatura_00066\"})";
	private final static String ONTOLOGY_QUERY_SQLLIKE = "select * from TestSensorTemperatura where Sensor.assetId = \"S_Temperatura_00066\"";
	private final static String ONTOLOGY_INSERT_SQLLIKE = "insert into TestSensorTemperatura(geometry, assetId, measure, timestamp) values (\"{ 'coordinates': [ 40.512967, -3.67495 ], 'type': 'Point' }\", \"S_Temperatura_00066\", 15, \"{ '$date': '2014-04-29T08:24:54.005Z'}\")";
						
	
	private SSAPResourceAPI api;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		FixtureLoader loader = new FixtureLoader();
		
		ONTOLOGY_INSTANCE = loader.load("ontology_instance");
		COMMAND_REQ_INSTANCE = loader.load("command_req_instance");
		ONTOLOGY_UPDATE = loader.load("ontology_update");
		ONTOLOGY_QUERY_NATIVE_CRITERIA = loader.load("ontology_quey_native_criteria");
		ONTOLOGY_DELETE = loader.load("ontology_delete");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.api = new SSAPResourceAPI(SERVICE_URL);
		
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testQueryBDC() {

		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		Response respQuery=this.api.query(sessionkey, null, "select identificacion from asset;", null, "BDC");		
		assertEquals(200, respQuery.getStatus());	
		
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "QUERY"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
			
		utils.leave(api, sessionkey);	
	}
	
	
	@Test
	public void testJoinByTokenLeave() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		Response respLeave = utils.leave(api, sessionkey);
		assertEquals(respLeave.getStatus(), 200);
	}
	
	@Test
	public void testSubscribeUnsubscribe2() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		Response respSubscribe = this.api.subscribe(sessionkey, ONTOLOGY_NAME, "select * from " + ONTOLOGY_NAME, 0, null, "SQLLIKE", "http://localhost:10080/ReceptorSuscripcionesRest/SubscriptionReceiver");
		String subscriptionId = "ggfgfgsglsfdfdfd";//utils.getSSAPResource(api, respSubscribe).getData();
		assertEquals(200, respSubscribe.getStatus());
		
		//TODO: Si hay conexion siempre devuelve 200 aunque el subscriptionId no exista. En cambio el sessionKey si que lo tiene en cuenta
		Response respUnsubscribe = this.api.unsubscribe(sessionkey, subscriptionId);
		assertEquals(200, respUnsubscribe.getStatus());
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respUnsubscribe.getStatus(), "UNSUBSCRIBE"));
		
		utils.leave(api, sessionkey);		
	}
	
	
	@Test
	public void testInsert() {
		
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		
		Response respInsert=this.api.insert(ssapInsert);
		assertEquals(200, respInsert.getStatus());
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respInsert.getStatus(), "INSERT"));
		
		String data = utils.getSSAPResource(api, respInsert).getData();
		//TODO: Respuesta no parseable
		//JsonObject jRespInsert = Json.createReader(new StringReader(data)).readObject();
		//assertEquals(true, jRespInsert.containsKey("ObjectId"));	
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, data));
		
		utils.leave(api, sessionkey);		
	}
	
	@Test
	public void testUpdate() throws JsonProcessingException, IOException {
		
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		
		Response respInsert=this.api.insert(ssapInsert);		
		String data = utils.getSSAPResource(api, respInsert).getData();
		JsonNode ObjectId = JSON.getObjectMapper().readTree(data);
			
		((ObjectNode)ONTOLOGY_UPDATE).replace("_id", ObjectId.at("/_id"));
			
		SSAPResource ssapUpdate=new SSAPResource();
		ssapUpdate.setSessionKey(sessionkey);
		ssapUpdate.setOntology(ONTOLOGY_NAME);
		ssapUpdate.setData(ONTOLOGY_UPDATE.toString());
		
		Response respUpdate=this.api.update(ssapUpdate);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respUpdate.getStatus(), "UPDATE"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respUpdate).getData()));
		assertEquals(respUpdate.getStatus(), 200);

		utils.leave(api, sessionkey);	
	}
	
	@Test
	public void testQueryByObjectId() throws JsonProcessingException, IOException {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);

		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);		
		String data = utils.getSSAPResource(api, respInsert).getData();				
		//TODO: Al no ser parseable toca realizar tratamiento de cadenas 
		JsonNode ObjectId = JSON.getObjectMapper().readTree(data);
		
		Response respQuery=this.api.query(ObjectId.at("/_id/$oid").asText(), sessionkey, ONTOLOGY_NAME);
		assertEquals(200, respQuery.getStatus());
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "QUERY_BY_ID"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
		
		utils.leave(api, sessionkey);	
	}
	
	@Test
	public void testQueryNativeCriteria() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);		
		
		Response respQuery=this.api.query(sessionkey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE_CRITERIA.toString(), null, "NATIVE");
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "QUERY_NATIVE_CRITEREIA"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testQueryNativeStatement() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);		
		
		Response respQuery=this.api.query(sessionkey, ONTOLOGY_NAME, ONTOLOGY_QUERY_NATIVE_STATEMENT, null, "NATIVE");
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "QUERY_NATIVE_STATEMENT"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testQuerySQLLIKEStatement() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);
				
		Response respQuery=this.api.query(sessionkey, ONTOLOGY_NAME, ONTOLOGY_QUERY_SQLLIKE, null, "SQLLIKE");
		
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "QUERY_SQL_LIKE"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api,respQuery).getData()));
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testInsertSQLLIKEStatement() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
			
		Response respQuery=this.api.query(sessionkey, ONTOLOGY_NAME, ONTOLOGY_INSERT_SQLLIKE, null, "SQLLIKE");
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "INSERT_SQL_LIKE"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
		
		utils.leave(api, sessionkey);
	}

	@Test
	public void testDeleteByObjectId() throws JsonProcessingException, IOException {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);
		String data = utils.getSSAPResource(api, respInsert).getData();	
		JsonNode ObjectId = JSON.getObjectMapper().readTree(data);
		
		Response respQuery=this.api.deleteOid(ObjectId.at("/_id/$oid").asText(), sessionkey, ONTOLOGY_NAME);
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "DELETE_BY_OBJECT_ID"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, respQuery.getStatus()));
		//log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api, respQuery).getData()));
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testDelete() throws  IOException {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(ONTOLOGY_INSTANCE.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
		Response respInsert=this.api.insert(ssapInsert);		
		String data = utils.getSSAPResource(api, respInsert).getData();				
		
		
		SSAPResource ssapDelete=new SSAPResource();
		ssapDelete.setData(data);
		ssapDelete.setSessionKey(sessionkey);
		ssapDelete.setOntology(ONTOLOGY_NAME);
		
		Response respQuery=this.api.delete(ssapDelete);
		
		assertEquals(respQuery.getStatus(), 200);
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respQuery.getStatus(), "DELETE"));
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testBulk() {
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		
		List<String> msgBulk = new ArrayList<String>();
		String insert_instance = ONTOLOGY_INSTANCE.toString();
		msgBulk.add(insert_instance);
		msgBulk.add(insert_instance);
		msgBulk.add(insert_instance);
		
		SSAPResource ssapInsert=new SSAPResource();
		ssapInsert.setData(msgBulk.toString());
		ssapInsert.setSessionKey(sessionkey);
		ssapInsert.setOntology(ONTOLOGY_NAME);
				
		Response respInsert=this.api.insert(ssapInsert);
		assertEquals(200, respInsert.getStatus());
		log.info(String.format(LogMessages.LOG_HHTP_RESPONSE_CODE, respInsert.getStatus(), "BULK_INSERT"));
		log.info(String.format(LogMessages.LOG_RESPONSE_DATA, utils.getSSAPResource(api,respInsert).getData()));
		
		utils.leave(api, sessionkey);
		
	}
	
	@Test
	public void testStatus() {
			
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		Map<String,String> status = new HashMap<String, String>();
		status.put("atributo1", "valor1");
		status.put("atributo2", "valor2");
		
		Response response = this.api.status(KP, KP_INSTANCE, TOKEN, "", status, timeStamp);
		
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testLocation() {
			
		Random rnd = new Random();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		double lat = (int) (rnd.nextDouble() * 50 + 35);
		double lon = (int) (rnd.nextDouble() * 20 + -3);
		double alt = (int) (rnd.nextDouble() * 50 + 35);
		double speed = (int) (rnd.nextDouble() * 100 + 0);
		
		Response response = this.api.location(KP, KP_INSTANCE, TOKEN, "", 1, lat, lon, alt, 1, speed, timeStamp);
	
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testError() {
			
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		String code = "ERROR_CODE";
		String message = "ERROR MESSAGE";
		
		Response response = this.api.error(KP, KP_INSTANCE, TOKEN, "", SSAPSeverityLevel.ERROR, code, message, timeStamp);
		
		assertEquals(200, response.getStatus());
				
	}
	
	@Test
	public void testLog() {
			
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
		
		String code = "ERROR_CODE";
		String message = "ERROR MESSAGE";
		
		Response response = this.api.log(KP, KP_INSTANCE, TOKEN, "", SSAPLogLevel.FATAL, code, message, timeStamp);
	
		assertEquals(200, response.getStatus());		
	}
	
	@Test
	public void testCommand() {
		
		Response respJoin = utils.join(this.api, KP_INSTANCE, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		Map<String,String> status = new HashMap<String, String>();
		status.put("atributo1", "valor1");
		status.put("atributo2", "valor2");
		Response response = this.api.command(sessionkey, KP, KP_INSTANCE, SSAPCommandType.STATUS, status);
	
		assertEquals(200, response.getStatus());	
		
		utils.leave(api, sessionkey);
	}
	
	@Test
	public void testSubscribeCommand() {
		Response response = this.api.subscribe_command(KP, KP_INSTANCE, TOKEN, SSAPCommandType.STATUS, "http://localhost:3003");
		assertEquals(200, response.getStatus());
	}
	
	// This test is only valid for local testing or enviorments where there isn't a proxy o router beetwen pc an sofia2
	@Test
	@Ignore
	public void testSubscribeCommandAndIndicattion() throws Exception  {
		String kpInstance =  KP_INSTANCE + UUID.randomUUID().toString();
		this.api.subscribe_command(KP, kpInstance, TOKEN, SSAPCommandType.STATUS, "http://localhost:3003");
		String indicationStr = "";

		//Start listening for http indication request
		ExecutorService exec = Executors.newFixedThreadPool(2);
		Future<String> indication = exec.submit(new LightHttpListener(3003));
		
		//Sending a command
		try {Thread.sleep(500);} catch (InterruptedException e) {}
		Response respJoin = utils.join(api, kpInstance, TOKEN);	
		String sessionkey = utils.getSSAPResource(api, respJoin).getSessionKey();
		Map<String,String> status = new HashMap<String, String>();
		status.put("atributo1", "valor1");
		status.put("atributo2", "valor2");
		api.command(sessionkey, KP, kpInstance, SSAPCommandType.STATUS, status);
		
		//Waitting for server response
		indicationStr = indication.get(5, TimeUnit.SECONDS);
		assertFalse(indicationStr.isEmpty());
		System.out.println(indicationStr);
		//SSAPResource resource =
		//JsonNode indicationJson = JSON.deserializeToJson(JSON.deserializeToJson(indicationStr).path("body").asText());
		//JsonNode b = indicationJson.path("data");
		//System.out.println(b.asText());
		
		
	}
}
