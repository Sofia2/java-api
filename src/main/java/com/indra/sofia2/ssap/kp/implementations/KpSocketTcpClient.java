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
package com.indra.sofia2.ssap.kp.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Base64;

import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.SocketTCPConnectionConfig;
import com.indra.sofia2.ssap.kp.encryption.XXTEA;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;
import com.indra.sofia2.ssap.kp.implementations.tcpip.connector.IConnectorMessageListener;
import com.indra.sofia2.ssap.kp.implementations.tcpip.core.TcpipConnector;
import com.indra.sofia2.ssap.kp.implementations.tcpip.exception.KPIConnectorException;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinUserAndPasswordMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

@Deprecated
public class KpSocketTcpClient extends KpToExtend implements IConnectorMessageListener {
		
	/**
     * Lock para bloquear un envio hasta recibir la respuesta sincrona del Sib
     */
    private final Lock lock = new ReentrantLock();
    
    /**
     * Cola para almacenar las respuestas que deben devolverse de manera sincrona
     */
    private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();
	
	private TcpipConnector connector;
	
	
	public KpSocketTcpClient(SocketTCPConnectionConfig config){
		super(config);
		try {
			this.connector=new TcpipConnector();
			Properties prop=new Properties();
			prop.setProperty(TcpipConnector.HOST, config.getHostSIB());
			prop.setProperty(TcpipConnector.DEFAULT_IPADDRESS, config.getHostSIB());
			prop.setProperty(TcpipConnector.PORT, ""+config.getPortSIB());
			
			this.connector.setProperties(prop);
			this.connector.addSIBMessageListener(this);
			
		} catch (IOException e) {
			log.error("Imposible crear conector TCP");
		}
	}
	
	@Override
	public void connect() throws ConnectionToSibException {
		try {
			this.initializeIndicationPool();
			this.connector.connect();
			this.joined=true;
		} catch (KPIConnectorException e) {
			this.joined=false;
			log.error("Imposible conetar con SIB por TCP");
			throw new ConnectionToSibException("Imposible conetar con SIB por TCP");
		}
		
	}	

	@Override
	public void disconnect() {
		try {
			this.destroyIndicationPool();
			this.connector.disconnect();
			this.joined=false;
		} catch (KPIConnectorException e) {
			this.joined=false;
			log.error("Imposible conetar con SIB por TCP");
			throw new ConnectionToSibException("Imposible conetar con SIB por TCP");
			
		}
		
	}

	@Override
	public SSAPMessage send(SSAPMessage msg) throws ConnectionToSibException {
		Callback callback = new Callback(); 
		//Bloquea hasta recibir la respuesta (SendToSib ofrece devolución sincrona de respuesta)
	    lock.lock();
	    try {
	      callbacks.add(callback);
	      this.connector.write(("<TCP_JSON>"+msg.toJson()+"</TCP_JSON>").getBytes());	
	    } finally {
	      lock.unlock();
	    }
	   
	    return SSAPMessage.fromJsonToSSAPMessage(callback.get());
	}
	
	@Override
	public SSAPMessage sendCipher(SSAPMessage msg)
			throws ConnectionToSibException {
	
		Callback callback = new Callback();
		//Bloquea hasta recibir la respuesta (SendToSib ofrece devolución sincrona de respuesta)
	    lock.lock();
	    try {
	      callbacks.add(callback);
	      
	      byte[] encrypted=XXTEA.encrypt((msg.toJson()).getBytes(), this.xxteaCipherKey.getBytes());
		    
		  byte[] bCifradoBase64;
		    
		  if(msg.getMessageType()==SSAPMessageTypes.JOIN){
		  	SSAPBodyJoinUserAndPasswordMessage body=SSAPBodyJoinUserAndPasswordMessage.fromJsonToSSAPBodyJoinUserAndPasswordMessage(msg.getBody());
		    	
		  	String kpName=body.getInstance().split(":")[0];
		    	
		  	String completeMessage=kpName.length()+"#"+kpName+Base64.encodeBase64String(encrypted);
		    	
		  	bCifradoBase64=completeMessage.getBytes();
		  }else{
		  	bCifradoBase64=Base64.encodeBase64(encrypted);
		  }
	      
		  String toSend="<TCP_JSON>"+new String(bCifradoBase64)+"</TCP_JSON>";
		  
	      this.connector.write(toSend.getBytes());	
	   
	    } finally {
	      lock.unlock();
	    }
	    
	    String response=callback.get();
		
	   
	    return SSAPMessage.fromJsonToSSAPMessage(response);
	
	
	}
	
	
	@Override
	public void messageReceived(byte[] message) {
		String msg=new String(message);
		
		msg=msg.replace("<TCP_JSON>", "");
		msg=msg.replace("</TCP_JSON>", "");
		
		msg= clearJsonMessage(msg);
		
    	
    	SSAPMessage ssapMessage = SSAPMessage.fromJsonToSSAPMessage(msg);
    	
		//Si el mensaje es un JOIN recupera el SessionKey
		if (ssapMessage.getMessageType().equals(SSAPMessageTypes.JOIN)) {
			String sessionKey = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody()).getData();
			this.sessionKey = sessionKey;
		}		
		//Si es una notificacion de suscripcion, la notifica al listener
    	if(ssapMessage.getMessageType() == SSAPMessageTypes.INDICATION){
    		//Envia la notificación al subscriptionListener correspondiente
    		String messageId=ssapMessage.getMessageId();

    		ArrayList<IndicationTask> tasks = new ArrayList<IndicationTask>();
    		
    		if(messageId!=null){
    			for (Iterator iterator = subscriptionListeners.iterator(); iterator.hasNext();) {
					Listener4SIBIndicationNotifications listener = (Listener4SIBIndicationNotifications)iterator.next();
					tasks.add(new IndicationTask(listener, messageId, ssapMessage));
				}
    			this.executeIndicationTasks(tasks);
    		}
    		/*
    		if(messageId!=null && subscriptionListeners.containsKey(messageId)){
    			Listener4SIBNotifications listener=subscriptionListeners.get(messageId);
    			
    			if(listener!=null) 
    				listener.onIndication(ssapMessage);
    		}
    		*/
    		
    	
    	}else{
    		//Cualquier otro lo devuelve a la cola de mensajes sincronos
    		callbacks.poll().handle(msg);
    	}
		
	}
	
	
	private String clearJsonMessage(String payload){
		//mensaje no cifrado con XXTEA
		if(payload.startsWith("{") &&
				payload.endsWith("}") && 
				payload.contains("direction") && 
				payload.contains("sessionKey")){
			 return payload;
			 
		}else{
			 byte[] bCifradoBaseado=Base64.decodeBase64(payload);
	         
			 for(int i=0;i<bCifradoBaseado.length;i++){
				 bCifradoBaseado[i]=(byte)(bCifradoBaseado[i] & 0xFF);
	         }
			 
			 String clearMessage=new String(XXTEA.decrypt(bCifradoBaseado, this.xxteaCipherKey.getBytes()));
			 
			 return clearMessage;
		}
	}
	
	
	
	 /**
     * Clase para recibir  las notificaciones sincronas de mensajes SSAP
     * @author jfgpimpollo
     *
     */
    static class Callback {

        private final CountDownLatch latch = new CountDownLatch(1);

        private String response;

        String get() {
          try {
            latch.await();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          return response;
        }

        void handle(String response) {
          this.response = response;
          latch.countDown();
        }
      }

	@Override
	public boolean isConnected() {
		return this.connector.isRunning();
	}
	
	@Override
	public boolean isConnectionEstablished() {
		return isConnected();
	}
	
	
}
