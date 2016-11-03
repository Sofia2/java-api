/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2014  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.indra.sofia2.ssap.kp.implementations;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.QoS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.implementations.mqtt.KpMQTTClient;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageGenerator;
import com.indra.sofia2.ssap.ssap.SSAPQueryType;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class KpMqttIsConnected {

	private static Log log = LogFactory.getLog(KpGatewayRestFuncional.class);

	private final static String HOST = "localhost";
	private final static int PORT = 1883;

	private final static String TOKEN = "0fd4788f28da4d2ca5c0804682ca18b2";
	private final static String KP_INSTANCE = "PRUEBAS:KPTestTemperatura01";
	private final static String ONTOLOGY_NAME = "PruebaRendimiento";
	
	private Kp kp;
	private String sessionKey;
	private MQTTConnectionConfig config;
	
	private final static String MQTTUSERNAME = "sofia2";
	private final static String MQTTPASSWORD = "indra2014";
	private final static boolean ENABLEMQTTAUTHENTICATION = false;

	@Before
	public void setUpBeforeClass() throws Exception {

		config = new MQTTConnectionConfig();
		config.setHostSIB(HOST);
		config.setPortSIB(PORT);
		config.setKeepAliveInSeconds(5);
		config.setQualityOfService(QoS.AT_LEAST_ONCE);
		config.setTimeOutConnectionSIB(5000);
		config.setSsapResponseTimeout(5000);
		config.setUser("pedro");
		config.setPassword("pedro");
		if (ENABLEMQTTAUTHENTICATION) {
			config.setUser(MQTTUSERNAME);
			config.setPassword(MQTTPASSWORD);
		}

		this.kp = new KpMQTTClient(config);

		this.kp.connect();
		performJoin();
		createSubscription();
	}

	private void performJoin() {
		SSAPMessage msgJoin = SSAPMessageGenerator.getInstance()
				.generateJoinByTokenMessage(TOKEN, KP_INSTANCE);

		log.info("Sending JOIN message to the SIB: " + msgJoin.toJson());

		// Envia el mensaje
		SSAPMessage response = kp.send(msgJoin);

		log.info("JOIN response: " + response.toJson());

		// Comprueba que el mensaje trae session key
		assertNotSame(response.getSessionKey(), null);
		sessionKey = response.getSessionKey();
		log.info("Session key: " + sessionKey);
	}
	
	private void sendData(){
		try {
			if (!kp.isConnectionEstablished()){
				kp.disconnect();
				kp = new KpMQTTClient(config);
				kp.connect();
			}
			performJoin();
		} catch (Throwable e){
			log.info("Unable to send data: ", e);
		};
	}
	
	private void createSubscription() throws IOException {
		kp.addListener4SIBNotifications(new Listener4SIBIndicationNotifications() {
			@Override
			public void onIndication(String messageId, SSAPMessage ssapMessage) {
				log.info("An INDICATION message has been received: " + ssapMessage.toJson());
			}
		});
		
		SSAPMessage msg=SSAPMessageGenerator.getInstance().generateSubscribeMessage(sessionKey, ONTOLOGY_NAME, 0, "", SSAPQueryType.SQLLIKE);
		log.info("Sending SUBSCRIBE message to the SIB: " + msg.toJson());
		SSAPMessage msgSubscribe = kp.send(msg);
		log.info("SUBSCRIBE response: " + msgSubscribe);
		SSAPBodyReturnMessage responseSubscribeBody = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(msgSubscribe.getBody());
		assertNotSame(responseSubscribeBody.getData(), null);
		assertTrue(responseSubscribeBody.isOk());
		assertSame(responseSubscribeBody.getError(), null);
	}

	@After
	public void disconnectAfterClass() throws Exception {
		this.kp.disconnect();
	}

	@Test
	public void testIsConnected() {
		for (int i = 0; i < 1000; i++){
			try {
				log.info("Checking physical connection status");
				boolean retval = this.kp.isConnectionEstablished();
				if (i % 2 == 0){
					sendData();
				}
				log.info("Status: " + (retval ? "OK" : "KO"));
				try {
					Thread.sleep(10000);
				} catch (Exception e){}
			} catch (Throwable e){
				log.info("An unexpected exception was raised");
				assertTrue(false);
			}
		}
	}

}
