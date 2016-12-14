package com.indra.sofia2.ssap.kp.implementations;

import java.util.UUID;

import org.fusesource.mqtt.client.QoS;

import com.indra.sofia2.ssap.kp.SSAPMessageGenerator;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSIBException;
import com.indra.sofia2.ssap.kp.implementations.mqtt.KpMQTTClient;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPQueryType;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class Api {

	private KpMQTTClient client;
	private String sessionKey;
	private Boolean releaseConnection=false;;
	
	public Api(String host, int port){
		MQTTConnectionConfig config = new MQTTConnectionConfig();
		config.setKeepAliveInSeconds(5);
		config.setQualityOfService(QoS.AT_LEAST_ONCE);
		config.setSibConnectionTimeout(5000);
		start(config, host, port);
	}
	
	public Api(String host, int port, int keepAlive, QoS qoS, int timeOut){
		MQTTConnectionConfig config = new MQTTConnectionConfig();
		config.setKeepAliveInSeconds(keepAlive);
		config.setQualityOfService(qoS);
		config.setSibConnectionTimeout(timeOut);
		start(config, host, port);
	}
	
	private void start(MQTTConnectionConfig config, String host, int port){
		config.setSibHost(host);
		config.setSibPort(port);
		client= new KpMQTTClient(config, "", "", "");
		client.disableStatusReport();
		
	}
	
	public Api joinAndDisconnect(String token, String kp){
		connect(token, kp);
		releaseConnection=true;
		return this;
	}
	
	public Api joinIfNoConnection(String token, String kp){
		connect(token, kp);
		releaseConnection=false;
		return this;
	}
	
	public void leave(){
		send(SSAPMessageGenerator.getInstance().generateLeaveMessage(sessionKey));
		if (client.isPhysicalConnectionEstablished()){
			client.disconnect();
		}
		sessionKey=null;
	}
	
	private void connect(String token, String kp) {
		try {
			if (!client.isPhysicalConnectionEstablished()){
					client.connect();
			}
			if (!client.isPhysicalConnectionEstablished()){
				SSAPMessage response = send(SSAPMessageGenerator.getInstance().generateJoinByTokenMessage(token,kp + ":" + UUID.randomUUID().toString()));
				sessionKey  = response.getSessionKey();
			}
		} catch (ConnectionToSIBException e) {
			e.printStackTrace();
		}
	}
	
	private void releaseConnection(){
		if (releaseConnection){
			leave();
		}
	}
	
	public SSAPBodyReturnMessage insert(String ontologia, String datos){
		SSAPMessage message =  send(SSAPMessageGenerator.getInstance().generateInsertMessage(sessionKey, ontologia, datos));
		releaseConnection();
		return (SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(message.getBody()));
	}
	
	public SSAPBodyReturnMessage query(String ontologia, String query, SSAPQueryType queryType){
		SSAPMessage message =  send(SSAPMessageGenerator.getInstance().generateQueryMessage(sessionKey, ontologia, query, queryType));
		releaseConnection();
		return (SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(message.getBody()));
	}
	
	public SSAPMessage send(SSAPMessage message) {
		SSAPMessage response = null;
		try {
			response = client.send(message);
		} catch (ConnectionToSIBException e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
}
