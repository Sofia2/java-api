/*******************************************************************************
 * Copyright 2013-15 Indra Sistemas S.A.
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

package com.indra.sofia2.ssap.kp.implementations.mqtt;

public class MqttConstants {

	/**
	 * Maximum MQTT ClientID length
	 */
	public static final int CLIENT_ID_LENGTH = 23;
	
	/**
	 * Topic for SSAP requests
	 */
	public static final String SIB_REQUESTS_TOPIC = "";
	
	/**
	 * Topic for SSAP responses
	 */
	private static final String PUBLICATION_TOPIC_PREFIX = "/TOPIC_MQTT_PUBLISH";

	/**
	 * Topic for SSAP INDICATION Messages
	 */
	private static final String SSAP_INDICATION_TOPIC_PREFIX = "/TOPIC_MQTT_INDICATION";
	
	/**
	 * Returns the MQTT topic used to receive SSAP responses.
	 */
	public static String getSsapResponseMqttTopic(String mqttClientId){
		return PUBLICATION_TOPIC_PREFIX + mqttClientId;
	}
	
	/**
	 * Returns the MQTT topic used to receive SSAP INDICATION messages.
	 * @param mqttClientId
	 * @return
	 */
	public static String getSsapIndicationMqttTopic(String mqttClientId){
		return SSAP_INDICATION_TOPIC_PREFIX + mqttClientId;
	}
}
