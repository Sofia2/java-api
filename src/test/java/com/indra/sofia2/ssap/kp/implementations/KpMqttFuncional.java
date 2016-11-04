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
package com.indra.sofia2.ssap.kp.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.fusesource.mqtt.client.QoS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.UnsupportedSSAPMessageTypeException;
import com.indra.sofia2.ssap.kp.implementations.mqtt.KpMQTTClient;
import com.indra.sofia2.ssap.ssap.SSAPBulkMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageGenerator;
import com.indra.sofia2.ssap.ssap.SSAPQueryType;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;
import com.indra.sofia2.ssap.ssap.body.bulk.message.SSAPBodyBulkReturnMessage;

public class KpMqttFuncional {

	private static final Logger log = LoggerFactory.getLogger(KpMqttFuncional.class);

	private final static String HOST = "<SIB_HOST>";
	private final static int PORT = 1883;

	private final static String TOKEN = "<TOKEN>";
	private final static String KP_INSTANCE = "<KP>";

	private final static String ONTOLOGY_NAME = "Sofia2InstanceStatus";
	private final static String ONTOLOGY_INSTANCE = "{\"statusData\":{ \"instanceId\":\"SsapApiTest\",\"systemStatusResult\":true,\"statusResult\":{\"cacheStatus\":true,\"cdbStatus\":true,\"hdbStatus\":true,\"rtdbStatus\":true,\"exportJobSchedulerStatus\":true,\"genericSchedulerStatus\":true,\"groupOntologySchedulerStatus\":true,\"mongoExportJobSchedulerStatus\":true,\"processStatus\":true,\"scriptStatus\":true,\"quartzStatus\":true,\"sibStatus\":true,\"hiveStatus\":true,\"hdfsStatus\":true},\"systemStatusTime\":{\"$date\": \"2014-01-30T17:14:00Z\"}}}";

	private final static String NATIVE_UPDATE = "{\"statusData\":{ \"instanceId\":\"SsapApiTest\",\"systemStatusResult\":true,\"statusResult\":{\"cacheStatus\":true,\"cdbStatus\":true,\"hdbStatus\":true,\"rtdbStatus\":true,\"exportJobSchedulerStatus\":true,\"genericSchedulerStatus\":true,\"groupOntologySchedulerStatus\":true,\"mongoExportJobSchedulerStatus\":true,\"processStatus\":true,\"scriptStatus\":true,\"quartzStatus\":true,\"sibStatus\":true,\"hiveStatus\":true,\"hdfsStatus\":true},\"systemStatusTime\":{\"$date\": \"2014-01-30T17:14:00Z\"}}}";
	private final static String NATIVE_UPDATE_QUERY = "{\"statusData.instanceId\": \"SsapApiTest\"}";
	private final static String NATIVE_QUERY = "{\"statusData.instanceId\": \"SsapApiTest\"}";
	private final static String NATIVE_DELETE_QUERY = "db.Sofia2InstanceStatus.remove(" + NATIVE_QUERY
			+ ");";

	private final static String SQLLIKE_ONTOLOGY_INSTANCE = "insert into Sofia2InstanceStatus(instanceId, statusResult, systemStatusResult, systemStatusTime) values ('SsapApiTestSqlLike', \"{'cacheStatus':true,'cdbStatus':true,'hdbStatus':true,'rtdbStatus':true,'exportJobSchedulerStatus':true,'genericSchedulerStatus':true,'groupOntologySchedulerStatus':true,'mongoExportJobSchedulerStatus':true,'processStatus':true,'scriptStatus':true,'quartzStatus':true,'sibStatus':true,'hiveStatus':true,'hdfsStatus':true}\", false, \"{ '$date': '2014-04-29T08:24:54.005Z'}\")";
	private final static String SQLLIKE_UPDATE = "update Sofia2InstanceStatus set systemStatusResult = true where statusData.instanceId=\"SsapApiTestSqlLike\"";
	private final static String SQLLIKE_UPDATE_QUERY = "select * from Sofia2InstanceStatus where statusData.instanceId = \"SsapApiTestSqlLike\"";
	private final static String SQLLIKE_DELETE_QUERY = "delete from Sofia2InstanceStatus where statusData.instanceId = \"SsapApiTestSqlLike\"";

	private final static String MQTT_USERNAME = "username";
	private final static String MQTT_PASSWORD = "password";
	private final static boolean ENABLE_MQTT_AUTHENTICATION = true;

	private Kp kp;
	private boolean indicationReceived;
	private String sessionKey;

	@Before
	public void setUpBeforeClass() throws Exception {

		MQTTConnectionConfig config = new MQTTConnectionConfig();
		config.setSibHost(HOST);
		config.setSibPort(PORT);
		config.setKeepAliveInSeconds(5);
		config.setQualityOfService(QoS.AT_LEAST_ONCE);
		config.setSibConnectionTimeout(6000);
		config.setSsapResponseTimeout(1000000);
		if (ENABLE_MQTT_AUTHENTICATION) {
			config.setUser(MQTT_USERNAME);
			config.setPassword(MQTT_PASSWORD);
		}
		kp = new KpMQTTClient(config);
		kp.connect();
		doJoin();
	}

	@After
	public void disconnectAfterClass() throws Exception {
		doLeave();
		kp.disconnect();
	}

	private void doJoin() throws Exception {
		SSAPMessage joinMessage = SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(TOKEN, KP_INSTANCE);
		log.info("Sending JOIN message to the SIB. Request = {}." + joinMessage.toJson());
		SSAPMessage response = kp.send(joinMessage);
		log.info("A JOIN response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				joinMessage.toJson());
		assertNotSame(response.getSessionKey(), null);
		log.info("A session key has been received. SessionKey = {}.", response.getSessionKey());
		sessionKey = response.getSessionKey();
		SSAPBodyReturnMessage bodyReturn = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertEquals(bodyReturn.getData(), sessionKey);
		assertTrue(bodyReturn.isOk());
		assertSame(bodyReturn.getError(), null);
	}

	private void doLeave() throws Exception {
		SSAPMessage leaveMessage = SSAPMessageGenerator.getInstance().generateLeaveMessage(sessionKey);
		log.info("Sending LEAVE message to the SIB. Request = {}." + leaveMessage.toJson());
		SSAPMessage response = kp.send(leaveMessage);
		log.info("A LEAVE response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				leaveMessage.toJson());
		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertEquals(responseBody.getData(), sessionKey);
		assertTrue(responseBody.isOk());
		assertSame(responseBody.getError(), null);
	}

	@Test
	public void testNativeInsert() throws Exception {
		SSAPMessage insertMessage = SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME,
				ONTOLOGY_INSTANCE);
		log.info("Sending native INSERT message to the SIB. Request = {}." + insertMessage.toJson());
		SSAPMessage response = kp.send(insertMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An INSERT response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				insertMessage.toJson());
	}

	@Test
	public void testNativeUpdate() throws Exception {
		SSAPMessage updateMessage = SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME,
				NATIVE_UPDATE, NATIVE_UPDATE_QUERY);
		log.info("Sending native UPDATE message to the SIB. Request = {}." + updateMessage.toJson());
		SSAPMessage response = kp.send(updateMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An UPDATE response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				updateMessage.toJson());
	}

	@Test
	public void testNativeQuery() throws Exception {
		SSAPMessage queryMessage = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME,
				NATIVE_QUERY);
		log.info("Sending native QUERY message to the SIB. Request = {}." + queryMessage.toJson());
		SSAPMessage response = kp.send(queryMessage);

		SSAPBodyReturnMessage returned = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(returned.isOk());
		log.info("An QUERY response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				queryMessage.toJson());
	}

	@Test
	public void testNativeDelete() throws Exception {
		SSAPMessage deleteMessage = SSAPMessageGenerator.getInstance().generateDeleteMessage(sessionKey, ONTOLOGY_NAME,
				NATIVE_DELETE_QUERY);
		log.info("Sending native DELETE message to the SIB. Request = {}." + deleteMessage.toJson());
		SSAPMessage response = kp.send(deleteMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An DELETE response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				deleteMessage.toJson());
	}

	@Test
	public void testSqlLikeInsert() throws Exception {
		SSAPMessage insertMessage = SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME,
				SQLLIKE_ONTOLOGY_INSTANCE, SSAPQueryType.SQLLIKE);
		log.info("Sending SQL-LIKE INSERT message to the SIB. Request = {}." + insertMessage.toJson());
		SSAPMessage response = kp.send(insertMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An INSERT response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				insertMessage.toJson());
	}

	@Test
	public void testSqlLikeUpdate() throws Exception {
		// Genera un mensaje de UPDATE
		SSAPMessage updateMessage = SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME,
				null, SQLLIKE_UPDATE, SSAPQueryType.SQLLIKE);
		log.info("Sending SQL-LIKE UPDATE message to the SIB. Request = {}." + updateMessage.toJson());
		SSAPMessage response = kp.send(updateMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An UPDATE response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				updateMessage.toJson());
	}

	@Test
	public void testSqlLikeQuery() throws Exception {
		SSAPMessage queryMessage = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ONTOLOGY_NAME,
				SQLLIKE_UPDATE_QUERY, SSAPQueryType.SQLLIKE);
		log.info("Sending SQL-LIKE QUERY message to the SIB. Request = {}." + queryMessage.toJson());
		SSAPMessage response = kp.send(queryMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An QUERY response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				queryMessage.toJson());
	}

	@Test
	public void testSqlLikeDelete() throws Exception {
		SSAPMessage deleteMessage = SSAPMessageGenerator.getInstance().generateDeleteMessage(sessionKey, ONTOLOGY_NAME,
				SQLLIKE_DELETE_QUERY, SSAPQueryType.SQLLIKE);
		log.info("Sending SQL-LIKE DELETE message to the SIB. Request = {}." + deleteMessage.toJson());
		SSAPMessage response = kp.send(deleteMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertTrue(responseBody.isOk());
		log.info("An QUERY response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				deleteMessage.toJson());
	}

	@Test
	public void testCdbQuery() throws Exception {
		SSAPMessage queryMessage = SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, null,
				"select * from Asset where identificacion='test'", SSAPQueryType.BDC);
		log.info("Sending CDB QUERY message to the SIB. Request = {}." + queryMessage.toJson());
		SSAPMessage response = kp.send(queryMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());
		assertEquals(responseBody.getError(), "Cannot find Asset");
		log.info("An CDB QUERY response has been received from the SIB. Response = {}, request = {}.",
				response.toJson(), queryMessage.toJson());
	}

	@Test
	public void testSubscribeUnsubscribe() throws Exception {

		log.info("Registering SIB notifications listener...");

		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {

			@Override
			public void onIndication(String messageId, SSAPMessage ssapMessage) {

				log.info("An INDICATION message has been received. SubscriptionId = {}, payload = {}.", messageId,
						ssapMessage.toJson());
				indicationReceived = true;
				SSAPBodyReturnMessage indicationMessage = SSAPBodyReturnMessage
						.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody());
				assertNotSame(indicationMessage.getData(), null);
				assertTrue(indicationMessage.isOk());
				assertSame(indicationMessage.getError(), null);
			}
		});

		SSAPMessage subscribeMessage = SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey,
				ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);

		log.info("Sending SUBSCRIBE message to the SIB. Request = {}." + subscribeMessage.toJson());
		SSAPMessage response = kp.send(subscribeMessage);

		SSAPBodyReturnMessage responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());

		assertNotSame(responseBody.getData(), null);
		assertTrue(responseBody.isOk());
		assertSame(responseBody.getError(), null);

		log.info("A SUBSCRIBE response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				subscribeMessage.toJson());

		String subscriptionId = responseBody.getData();
		testSqlLikeInsert();

		Thread.sleep(5000);
		assertTrue(indicationReceived);

		SSAPMessage unsubscribeMessage = SSAPMessageGenerator.getInstance().generateUnsubscribeMessage(sessionKey,
				ONTOLOGY_NAME, subscriptionId);

		log.info("Sending UNSUBSCRIBE message to the SIB. Request = {}." + unsubscribeMessage.toJson());

		response = kp.send(unsubscribeMessage);
		responseBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(response.getBody());

		assertEquals(responseBody.getData(), "");
		assertTrue(responseBody.isOk());
		assertSame(responseBody.getError(), null);

		log.info("An UNSUBSCRIBE response has been received from the SIB. Response = {}, request = {}.",
				response.toJson(), unsubscribeMessage.toJson());

	}

	@Test
	public void testBulk() throws Exception {

		SSAPMessage insertMessage1 = SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME,
				ONTOLOGY_INSTANCE);
		SSAPMessage insertMessage2 = SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME,
				ONTOLOGY_INSTANCE);
		SSAPMessage insertMessage3 = SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ONTOLOGY_NAME,
				SQLLIKE_ONTOLOGY_INSTANCE, SSAPQueryType.SQLLIKE);
		SSAPMessage updateMessage1 = SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME,
				NATIVE_UPDATE, NATIVE_UPDATE_QUERY);
		SSAPMessage updateMessage2 = SSAPMessageGenerator.getInstance().generateUpdateMessage(sessionKey, ONTOLOGY_NAME,
				null, SQLLIKE_UPDATE, SSAPQueryType.SQLLIKE);
		SSAPMessage deleteMessage1 = SSAPMessageGenerator.getInstance().generateDeleteMessage(sessionKey, ONTOLOGY_NAME,
				NATIVE_DELETE_QUERY);
		SSAPMessage deleteMessage2 = SSAPMessageGenerator.getInstance().generateDeleteMessage(sessionKey, ONTOLOGY_NAME,
				SQLLIKE_DELETE_QUERY, SSAPQueryType.SQLLIKE);

		SSAPBulkMessage bulkMessage = SSAPMessageGenerator.getInstance().generateBulkMessage(sessionKey);
		try {
			bulkMessage.addMessage(insertMessage1).addMessage(insertMessage2).addMessage(insertMessage3)
					.addMessage(updateMessage1).addMessage(updateMessage2).addMessage(deleteMessage1)
					.addMessage(deleteMessage2);
		} catch (UnsupportedSSAPMessageTypeException e) {
			e.printStackTrace();
		}

		log.info("Sending BULK message to the SIB. Request = {}." + bulkMessage.toJson());
		SSAPMessage response = kp.send(bulkMessage);

		SSAPBodyReturnMessage bodyBulkReturn = SSAPBodyReturnMessage
				.fromJsonToSSAPBodyReturnMessage(response.getBody());
		SSAPBodyBulkReturnMessage summary = SSAPBodyBulkReturnMessage
				.fromJsonToSSAPBodyBulkReturnMessage(bodyBulkReturn.getData());

		assertEquals(3, summary.getInsertSummary().getObjectIds().size());
		assertEquals(2, summary.getUpdateSummary().getObjectIds().size());
		assertEquals(2, summary.getDeleteSummary().getObjectIds().size());

		log.info("A BULK response has been received from the SIB. Response = {}, request = {}.", response.toJson(),
				bulkMessage.toJson());
	}
}
