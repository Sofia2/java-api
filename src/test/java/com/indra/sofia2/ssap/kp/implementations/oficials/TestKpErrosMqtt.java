package com.indra.sofia2.ssap.kp.implementations.oficials;

import org.fusesource.mqtt.client.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.Kp;
import com.indra.sofia2.ssap.kp.KpToExtend;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.implementations.mqtt.KpMQTTClient;
import com.indra.sofia2.ssap.kp.implementations.oficials.abstracts.KpErrorsAbstract;
import com.indra.sofia2.ssap.testutils.TestProperties;

public class TestKpErrosMqtt extends KpErrorsAbstract {
	
	private final static String HOST = TestProperties.getInstance().get("test.officials.mqtt.url");
	private final static int PORT = Integer.parseInt(TestProperties.getInstance().get("test.officials.mqtt.port"));
		
	private final static String MQTT_USERNAME = TestProperties.getInstance().get("test.officials.mqtt.username");
	private final static String MQTT_PASSWORD = TestProperties.getInstance().get("test.officials.mqtt.password");
	private final static boolean ENABLE_MQTT_AUTHENTICATION = Boolean.valueOf(TestProperties.getInstance().get("test.officials.mqtt.enable_athentication"));


	@Override
	public KpToExtend getImplementation() {
		
		MQTTConnectionConfig config = new MQTTConnectionConfig();
		config.setSibHost(HOST);
		config.setSibPort(PORT);
		config.setKeepAliveInSeconds(5);
		config.setQualityOfService(QoS.AT_LEAST_ONCE);
		config.setSibConnectionTimeout(Integer.valueOf(TestProperties.getInstance().get("test.officials.mqtt.connection_timeout")));
		config.setSsapResponseTimeout(Integer.valueOf(TestProperties.getInstance().get("test.officials.mqtt.response_timeout")));
		if (ENABLE_MQTT_AUTHENTICATION) {
			config.setUser(MQTT_USERNAME);
			config.setPassword(MQTT_PASSWORD);
		}
		
		return (new KpMQTTClient(config, KP, KP_INSTANCE, TOKEN));
	}

	@Override
	public Logger  getLog() {
		return LoggerFactory.getLogger(TestKpFunctionalMqtt.class);
	}	

}
