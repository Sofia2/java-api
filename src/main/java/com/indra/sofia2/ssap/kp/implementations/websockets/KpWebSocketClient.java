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
package com.indra.sofia2.ssap.kp.implementations.websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.OptionsBuilder;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.Socket.STATUS;

import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.WebSocketConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;
import com.indra.sofia2.ssap.kp.exceptions.SSAPResponseTimeoutException;
import com.indra.sofia2.ssap.kp.implementations.KpToExtend;
import com.indra.sofia2.ssap.kp.implementations.listener.KpConnectorEventListener;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class KpWebSocketClient extends KpToExtend {
	private Client client;
	private RequestBuilder request;
	private Socket socket;
	
	private KpConnectorEventListener connectionEventListener;
	protected int ssapKeepAlive;
	
	/**
     * Lock to block a request until receive the synchronous response from SIB
     */
    private final Lock lock = new ReentrantLock();
    
    /**
     * Queue to store ssap message responses
     */
    private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();
    //Correc: Renombrada variable local
	public KpWebSocketClient(WebSocketConnectionConfig config) throws ConnectionConfigException {
		super(config);
		this.ssapResponseTimeout=config.getTimeOutConnectionSIB();

		this.ssapKeepAlive=config.getKeepAliveInSeconds();

		this.client = ClientFactory.getDefault().newClient();		

		this.request = client.newRequestBuilder()
                .method(config.getMethod())
                .uri(config.getEndpointUri())
                .encoder(new Encoder<SSAPMessage, String>() {
                    @Override
                    public String encode(SSAPMessage data) {
                       return data.toJson();
                     }
                })
                .decoder(new Decoder<String, SSAPMessage>() {
                    @Override
                    public SSAPMessage decode(Event type, String data) {
                        String dataAux = data.trim();
                        // Padding
                        if (dataAux.length() == 0) {
                            return null;
                        }

                        if (type.equals(Event.MESSAGE)) {
                            return SSAPMessage.fromJsonToSSAPMessage(dataAux);
                        } else {
                            return null;
                        }
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.SSE)
                .transport(Request.TRANSPORT.LONG_POLLING);
	}
	
	public KpWebSocketClient(WebSocketConnectionConfig config, KpConnectorEventListener eventListener) throws ConnectionConfigException {
		this(config);
		this.connectionEventListener=eventListener;
	}

	@Override
	public void connect() throws ConnectionToSibException {
		this.initializeIndicationPool();

		OptionsBuilder newOptionsBuilder = this.client.newOptionsBuilder();
		newOptionsBuilder.requestTimeoutInSeconds( this.ssapKeepAlive );

		this.socket = this.client.create( newOptionsBuilder.build() );

		try{
			socket.on("message", new Function<SSAPMessage>() {
	            @Override
	            public void on(SSAPMessage message) {
	            	if(message.getMessageType()!=SSAPMessageTypes.INDICATION){
	            		//Si el mensaje es un JOIN recupera el SessionKey
						try{
							if (message.getMessageType()==SSAPMessageTypes.JOIN) {
								String sKey = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(message.getBody()).getData();
								sessionKey = sKey;
								if( SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(message.getBody()).isOk()&&
										sKey!=null) {
									joined=true;
								}
							}else if (message.getMessageType().equals(SSAPMessageTypes.LEAVE) &&
									SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(message.getBody()).isOk()){
								
								joined=false;
							}	
						
							//Notifies the reception to unlock the synchronous waiting
						}catch(Exception e){};
						
	            		callbacks.poll().handle(message.toJson());
	            		
	            	}else{
            			String messageId=message.getMessageId();
            			ArrayList<IndicationTask> tasks = new ArrayList<IndicationTask>();
						if(messageId!=null){
							//Notifica a los listener de las suscripciones hechas manualmente
			    			for (Iterator<Listener4SIBIndicationNotifications> iterator = subscriptionListeners.iterator(); iterator.hasNext();) {
								Listener4SIBIndicationNotifications listener = iterator.next();
								tasks.add(new IndicationTask(listener, messageId, message));
							}
			    			KpWebSocketClient.this.executeIndicationTasks(tasks);
						}
	            	}
	            }
	        }).on(new Function<Throwable>() {
	            @Override
	            public void on(Throwable t) {
	            	if(connectionEventListener!=null){
	            		connectionEventListener.onError(t);
	            	}
	            }
	        }).on(Event.CLOSE.name(), new Function<String>() {
	            @Override
	            public void on(String t) {
	            	if(connectionEventListener!=null){
	            		connectionEventListener.onClose(t);
	            	}
	            }
	        }).on(Event.OPEN.name(), new Function<String>() {
                @Override
                public void on(String t) {
                	if(connectionEventListener!=null){
                		connectionEventListener.onOpen(t);
                	}
                }
            }).open(this.request.build(),this.config.getTimeOutConnectionSIB(), TimeUnit.MILLISECONDS);
		}catch(Exception e){
			throw new ConnectionToSibException(e);
		}
	}
	
	
	@Override
	public boolean isConnected() {
		return isConnectionEstablished();
	}
	
	@Override
	public boolean isConnectionEstablished() {
		// TODO Auto-generated method stub
		return this.socket!=null && (this.socket.status()==STATUS.OPEN || this.socket.status()==STATUS.REOPENED);
	}

	@Override
	public void disconnect() {
		this.destroyIndicationPool();
		this.socket.close();
		
	}

	@Override
	public SSAPMessage send(SSAPMessage msg) throws ConnectionToSibException {
		try {

			//Send the message to Server
			Callback callback = new Callback(); 
		    lock.lock();//Blocks until receive the ssap response
			 try {
			      callbacks.add(callback);
			      this.socket.fire(msg);
			 }finally {
			      lock.unlock();
			 }
			 SSAPMessage responseSsap = SSAPMessage.fromJsonToSSAPMessage(callback.get());
			 			 
			 return responseSsap;
			
		} catch (IOException e) {
			throw new ConnectionToSibException(e);
		}
	}

	@Override
	public SSAPMessage sendCipher(SSAPMessage msg)
			throws ConnectionToSibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void messageReceived(byte[] message) {
		// TODO Auto-generated method stub
		
	}
	

	/**
     * Internal class to receive the synchronous notifications for SSAP messages
     * @author jfgpimpollo
     *
     */
    static class Callback {

        private final CountDownLatch latch = new CountDownLatch(1);

        private String response;

        String get() {
          try {
            latch.await(ssapResponseTimeout, TimeUnit.MILLISECONDS);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if(response==null)
        	  throw new SSAPResponseTimeoutException("Timeout: "+ssapResponseTimeout+" waiting for SSAP Response");
          return response;
        }

        void handle(String response) {
          this.response = response;
          latch.countDown();
        }
      }

}