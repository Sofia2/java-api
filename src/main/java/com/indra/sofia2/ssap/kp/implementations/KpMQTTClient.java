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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Base64;
import org.fusesource.hawtdispatch.transport.SslTransport;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.Topic;
import org.apache.log4j.Logger;

import com.indra.sofia2.ssap.kp.Listener4SIBIndicationNotifications;
import com.indra.sofia2.ssap.kp.config.MQTTConnectionConfig;
import com.indra.sofia2.ssap.kp.encryption.XXTEA;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionConfigException;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;
import com.indra.sofia2.ssap.kp.exceptions.DisconnectFromSibException;
import com.indra.sofia2.ssap.kp.exceptions.SSAPResponseTimeoutException;
import com.indra.sofia2.ssap.kp.implementations.mqtt.exceptions.MQTTClientNotConfiguredException;
import com.indra.sofia2.ssap.kp.implementations.utils.IndicationTask;
import com.indra.sofia2.ssap.ssap.SSAPMessage;
import com.indra.sofia2.ssap.ssap.SSAPMessageTypes;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinUserAndPasswordMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyReturnMessage;

public class KpMQTTClient extends KpToExtend {
	private static Logger log = Logger.getLogger(KpMQTTClient.class.getName());
	
	
	public static final int CLIENT_ID_LENGTH = 23;
	private static final int DEFAULT_DISCONNECTION_TIMEOUT=5000;
	
	private SSLContext sslContext;

	/**
	 * MQTT client to be used by the protocol to connect it to the MQTT server
	 * in SIB
	 */
	private MQTT clientMQTT;
	
//	/**
//	 * ClientId prefix to be used
//	 */
//	private String clientIdPrefix;

	/**
	 * MQTT connection between the MQTT client and the MQTT server in SIB
	 */
	private FutureConnection mqttConnection;

	/**
	 * Thread to receive SIB notifications, regardless if it is SSAP or not
	 */
	private SubscriptionThread subscriptionThread;

	/**
	 * Lock to block a request until receive the synchronous response from SIB
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * Queue to store ssap message responses
	 */
	//private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();
	private Callback responseCallback=null;
	

	/**
	 * Topic for SSAP responses
	 */
	public static final String TOPIC_PUBLISH_PREFIX = "/TOPIC_MQTT_PUBLISH";

	/**
	 * Topic for SSAP INDICATION Messages
	 */
	public static final String TOPIC_SUBSCRIBE_INDICATION_PREFIX = "/TOPIC_MQTT_INDICATION";
	
	/**
	 * MessageId for each mqtt message sent to SIB
	 */
	private long messageId=0;

	/**
	 * Constructor
	 * 
	 * @param config
	 * @throws ConnectionConfigException
	 */
	public KpMQTTClient(MQTTConnectionConfig config) throws ConnectionConfigException{
		super(config);	
	}

	

	/**
	 * Create a MQTT client and connects it to the MQTT server in SIB
	 */
	@Override
	public void connect() throws ConnectionToSibException {
		
		String sibAddress=null;
		try {
			log.info("Realiza conexión a Sofia2");
			
			MQTTConnectionConfig cfg = (MQTTConnectionConfig) config;
			
			
			try{
				if(config.getHostSIB().startsWith("tcp://")||config.getHostSIB().startsWith("ssl://")){
					InetAddress.getByName(config.getHostSIB().substring(6));
				}else{
					InetAddress.getByName(config.getHostSIB());
				}
				sibAddress=config.getHostSIB();
			}catch(java.net.UnknownHostException e){
				if(cfg.getDnsFailHostSIB()==null || cfg.getDnsFailHostSIB().trim().length()==0){
					log.warn("No se puede resolver la direccion: "+config.getHostSIB());
					logNetIsAvailable();
					throw new ConnectionToSibException("No se puede resolver la direccion: "+config.getHostSIB());
				}else{
					sibAddress=cfg.getDnsFailHostSIB();
					log.info("No se puede resolver la direccion: "+config.getHostSIB()+". Se utiliza direccion IP fija: "+cfg.getDnsFailHostSIB());
				}
			}
			
			this.initializeIndicationPool();
			
			
			
			ssapResponseTimeout = cfg.getSsapResponseTimeout();
			boolean cleanSession = cfg.isCleanSession();
					
			//Creates the client and open a connection to the SIB
			if (clientMQTT==null) {
				clientMQTT = new MQTT();
				
//				this.clientIdPrefix = cfg.getClientIdPrefix();
				// TODO: integrados username y password MQTT	
				String username = cfg.getUser();
				String password = cfg.getPassword();
				if (username != null){
					clientMQTT.setUserName(username);
				}
				if (password != null){
					clientMQTT.setPassword(password);
				}
				
				if(cfg.getClientId()==null){
					clientMQTT.setClientId(MQTTConnectionConfig.generateClientId());
				}else{
					if(cfg.getClientId().length() > CLIENT_ID_LENGTH){
						log.info("Se ha acortado el clientId a la longitud maxima: "+CLIENT_ID_LENGTH);
						clientMQTT.setClientId(cfg.getClientId().substring(0, CLIENT_ID_LENGTH));
					}else{
						clientMQTT.setClientId(cfg.getClientId());
					}
				}
					
				
				if ( sibAddress.startsWith( "ssl://" ) ) {

					// Inicializar solo una vez
					if ( sslContext == null ) {
						sslContext = initSSLContext();
					}
					
					clientMQTT.setHost( sibAddress + ":" + config.getPortSIB() );
					clientMQTT.setSslContext( sslContext );
				}
				else{
					clientMQTT.setHost(sibAddress,config.getPortSIB());
				}
				
				
				// Deshabilitar sistema de reconexión embebido en el cliente
				clientMQTT.setReconnectAttemptsMax(cfg.getReconnectAttemptsMax());
				clientMQTT.setConnectAttemptsMax(cfg.getConnectAttemptsMax());
				clientMQTT.setReconnectDelay(cfg.getReconnectDelay());
				clientMQTT.setReconnectDelayMax(cfg.getReconnectDelayMax());
				clientMQTT.setReconnectBackOffMultiplier(cfg.getReconnectBackOffMultiplier());
				clientMQTT.setReceiveBufferSize(cfg.getReceiveBufferSize());
				clientMQTT.setSendBufferSize(cfg.getSendBufferSize());
				clientMQTT.setTrafficClass(cfg.getTrafficClass());
				clientMQTT.setMaxReadRate(cfg.getMaxReadRate());
				clientMQTT.setMaxWriteRate(cfg.getMaxWriteRate());
				clientMQTT.setKeepAlive((short)cfg.getKeepAliveInSeconds());
				
			}
			if (mqttConnection==null ||  !mqttConnection.isConnected()) {
				clientMQTT.setCleanSession(cleanSession);
				mqttConnection = clientMQTT.futureConnection();
				
				int timeout=config.getTimeOutConnectionSIB();
				if (timeout == 0) {
					mqttConnection.connect().await();
				} else {
					mqttConnection.connect().await(timeout, TimeUnit.MILLISECONDS);
				}
			}
			
			
			//Subscribes to the KP in order to receive any kind of SIB notifications
			this.subscribeToNotificationTopics();
			
			//Notifica que se ha realizado la conexión a los listener
			if(this.isConnectionEstablished()){
				this.notifyConnection();
			}
			
			log.info("Conexión realizada");
			
		} catch (URISyntaxException e) {
			log.error("Error connecting to SIB on "+sibAddress+":"+config.getPortSIB(),e);
			this.disconnect();
			throw new ConnectionToSibException("Error connecting to SIB on "+sibAddress+":"+config.getPortSIB(),e);
		}
		catch (Exception e) {
			log.error("Error connecting to SIB on "+sibAddress+":"+config.getPortSIB(),e);
			this.disconnect();
			logNetIsAvailable();
			throw new ConnectionToSibException("Error connecting to SIB on "+sibAddress+":"+config.getPortSIB(),e);
		}

	}

	private SSLContext initSSLContext() throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		String keyStore=System.getProperty("javax.net.ssl.keyStore");
		String keyStorePassword=System.getProperty("javax.net.ssl.keyStorePassword");
		
		if(algorithm==null || algorithm.trim().length()==0){
			throw new ConnectionToSibException("System property: ssl.KeyManagerFactory.algorithm cannot be null or empty");
		}if(keyStore==null || keyStore.trim().length()==0){
			throw new ConnectionToSibException("System property: javax.net.ssl.keyStore cannot be null or empty");
		}
		if(keyStorePassword==null || keyStorePassword.trim().length()==0){
			throw new ConnectionToSibException("System property: avax.net.ssl.keyStorePassword cannot be null or empty");
		}
		
		
		FileInputStream fin = new FileInputStream(keyStore);
		KeyStore ks= KeyStore.getInstance("JKS");
		ks.load(fin, keyStorePassword.toCharArray());
							
		KeyManagerFactory kmf=KeyManagerFactory.getInstance(algorithm);
		kmf.init(ks, keyStorePassword.toCharArray());
		
		TrustManagerFactory tmf=TrustManagerFactory.getInstance(algorithm);
		tmf.init(ks);
		
		SSLContext sslContext=SSLContext.getInstance(SslTransport.protocol("ssl"));
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),  null);
		
		return sslContext;
	}
	
	@Override
	public boolean isConnected() {
		return isJoined() && isConnectionEstablished();
	}
	
	@Override
	public boolean isConnectionEstablished() {
		return mqttConnection != null && mqttConnection.isConnected();
	}
	
	public boolean isJoined(){
		return joined;
	}

	/**
	 * Disconnect the MQTT client from the SIB
	 */
	@Override
	public synchronized void disconnect() {
		log.info("Solicita desconexión del cliente MQTT");
	
		if (mqttConnection != null) {

			//Cierra el hilo de recepción
			try {
				// Stop the thread that waits for notifications from SIB
				if (this.subscriptionThread != null
						&& !this.subscriptionThread.isStoped()) {
					this.subscriptionThread.myStop();
				}
			}catch(Exception e){
				log.error("Error stopping subscriptions Thread", e);
			}
			
			//Se desuscribe de topicos de notificacion
			try{
				// Unsubscribe SIB notifications
				if(mqttConnection.isConnected()){
					unSubscribeToNotificationTopics();
				}
			}catch(Exception e){
				log.warn("Cannot unsubscribe if not connected");
			}
			
			//Cierra la conexión física
			try{
				
//				if(mqttConnection.isConnected()){
					int timeout=config.getTimeOutConnectionSIB();
					if (timeout == 0) {
						mqttConnection.kill().await(DEFAULT_DISCONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
					} else {
						try{
							mqttConnection.kill().await(timeout, TimeUnit.MILLISECONDS);
						}catch(Exception e){
							log.warn("Timeout disconnecting");
							mqttConnection.disconnect().await(timeout, TimeUnit.MILLISECONDS);
						}
					}
//				}
				
				log.info("Conexión cerrada correctamente");
			} catch(Exception e){
				log.error("Ignoring Error disconnect:" + e.getMessage());
			} finally {
				joined = false;
				mqttConnection=null;
				this.destroyIndicationPool();
				this.notifyDisconnection();
			}
			
		}else{
			log.info("La conexión ya estaba cerrada");
			joined = false;
			this.destroyIndicationPool();
			this.notifyDisconnection();
		}

	}
	

	/**
	 * Subscribe the MQTT client to the topics that the SIB will use to notify
	 * messages from SIB
	 */
	private void subscribeToNotificationTopics() {
		Future<byte[]> subscribeFuture = null;

		// Subscription to topic for ssap response messages
		try {
			Topic[] topics = { new Topic(TOPIC_PUBLISH_PREFIX+clientMQTT.getClientId().toString(), ((MQTTConnectionConfig)config).getQualityOfService()) };
			subscribeFuture = mqttConnection.subscribe(topics);

			int timeout=config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}			
		} catch (Exception e) {
			log.error("Suscription Error: " + e.getMessage());
			throw new ConnectionToSibException(e);
		}

		// Subscription to topic for ssap indication messages
		try {
			Topic[] topics = { new Topic(TOPIC_SUBSCRIBE_INDICATION_PREFIX+clientMQTT.getClientId().toString(), ((MQTTConnectionConfig)config).getQualityOfService()) };
			subscribeFuture = mqttConnection.subscribe(topics);

			int timeout=config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();
			} else {
				subscribeFuture.await(timeout, TimeUnit.MILLISECONDS);
			}	
		} catch (Exception e) {
			log.error("Suscription Error: " + e.getMessage());
			throw new ConnectionToSibException(e);
		}
		
				
		//Launch a Thread to receive notifications
		if(this.subscriptionThread==null || this.subscriptionThread.isStoped()){
			this.subscriptionThread=new SubscriptionThread();
			this.subscriptionThread.start();
		}

	}

	/**
	 * Unsubscribe the MQTT client to the topics that the SIB will use to notify
	 * messages from SIB
	 */
	private void unSubscribeToNotificationTopics() {
		Future<Void> subscribeFuture = null;

		// Unsubscription to topic for ssap response messages
		try {
			String[] topics = { new String(TOPIC_PUBLISH_PREFIX+clientMQTT.getClientId().toString()) };
			
			subscribeFuture = mqttConnection.unsubscribe(topics);
			int timeout = config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();				
			}
			else {
				subscribeFuture.await( timeout, TimeUnit.MILLISECONDS );
			}

		} catch (Exception e) {
			log.warn("Cannot unsubscribe if not conected");
		}

		// Unsubscription to topic for ssap indication messages
		try {
			String[] topics = { new String(TOPIC_SUBSCRIBE_INDICATION_PREFIX+clientMQTT.getClientId().toString()) };
			subscribeFuture = mqttConnection.unsubscribe(topics);
			int timeout = config.getTimeOutConnectionSIB();
			if (timeout == 0) {
				subscribeFuture.await();				
			}
			else {
				subscribeFuture.await( timeout, TimeUnit.MILLISECONDS );
			}

		} catch (Exception e) {
			log.warn("Cannot unsubscribe if not conected");
		}

	}

	/**
	 * Send a SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage send(SSAPMessage msg) throws ConnectionToSibException {
		log.info("Envia mensaje a Sofia2");
		logNetIsAvailable();
		// Sends the message to Server
		try {
			Callback callback = new Callback();
			SSAPMessage responseSsap;
			synchronized(this){
				this.responseCallback=callback;
				    
			    //Publish a QoS message
			    //It is not necessary publish topic. The message will be received by the handler in server
			    mqttConnection.publish("", msg.toJson().getBytes(), ((MQTTConnectionConfig)config).getQualityOfService(), false);
			    
				responseSsap = SSAPMessage.fromJsonToSSAPMessage(callback.get());
				responseCallback=null;
			}
			log.info("Mensaje enviado correctamente");
			return responseSsap;

		}catch (RuntimeException e){
			log.error("Error Enviando mensaje a Sofia2: " + e.getMessage(), e);
			responseCallback=null;
			logNetIsAvailable();
			throw new ConnectionToSibException(e);
		}catch (Exception e) {
			log.error("Error Enviando mensaje a Sofia2" + e.getMessage(), e);
			responseCallback=null;
			logNetIsAvailable();
			throw new ConnectionToSibException(e);
		}
	}

	/**
	 * Send a SSAP message to the server, and returns the response
	 */
	@Override
	public SSAPMessage sendCipher(SSAPMessage msg) throws ConnectionToSibException {
		
		//Sends the message to Server
		try {

			Callback callback = new Callback();
			lock.lock();// Blocks until receive the ssap response
			try {
				//callbacks.add(callback);
				this.responseCallback=callback;

				byte[] encrypted = XXTEA.encrypt(msg.toJson().getBytes(),
						this.xxteaCipherKey.getBytes());

				byte[] bCifradoBase64;

				if (msg.getMessageType() == SSAPMessageTypes.JOIN) {
					SSAPBodyJoinUserAndPasswordMessage body = SSAPBodyJoinUserAndPasswordMessage
							.fromJsonToSSAPBodyJoinUserAndPasswordMessage(msg
									.getBody());

					String kpName = body.getInstance().split(":")[0];

					String completeMessage = kpName.length() + "#" + kpName
							+ Base64.encodeBase64String(encrypted);

					bCifradoBase64 = completeMessage.getBytes();
				} else {
					bCifradoBase64 = Base64.encodeBase64(encrypted);
				}

				// Publish a QoS message
				// It is not necessary publish topic. The message will be
				// received by the handler in server
				mqttConnection.publish("", bCifradoBase64,
						((MQTTConnectionConfig) config).getQualityOfService(),
						false);

			} finally {
				lock.unlock();
			}

			SSAPMessage responseSsap = SSAPMessage
					.fromJsonToSSAPMessage(callback.get());

			return responseSsap;

		} catch (Exception e) {
			log.error("Publication Error: " + e.getMessage(), e);
			throw new ConnectionToSibException(e);
		}
	}

	/**
	 * This thread will be continuously running to receive any kind of messages
	 * from SIB
	 * 
	 * @author jfgpimpollo
	 *
	 */
	private class SubscriptionThread extends Thread {
		private Future<Message> receive;

		private Boolean stop = false;

		protected SubscriptionThread() {

		}

		protected void myStop() {
			this.stop = true;
			if ( this.receive != null ) {
				this.interrupt();
			}
		}

		protected boolean isStoped() {
			return stop;

		}
		

		@Override
	    public void run() {
			stop=false;
			while (!stop){
				Message message =null;
				try{
					receive = mqttConnection.receive();					
					// verify the reception
					message = receive.await();
					log.info("Recibido mensaje desde Sofia2");
				}catch(InterruptedException e){
					message=null;
					if(responseCallback!=null){
						responseCallback.handle(null);
					}
					if(!stop){
						stop=true;
						disconnect();
					}
				}catch (Exception e){
					message=null;
					log.error("Error recibiendo mensaje en hilo de espera", e);
					log.info("Incia proceso de desconexión");
					if(responseCallback!=null){
						responseCallback.handle(null);
					}
					
					//Se detiene el hilo de espera
					stop=true;
					disconnect();
				}
				try{
					if ( message != null ) {
						message.ack();
						
						String messageTopic=message.getTopic();
						if(messageTopic.equals(TOPIC_PUBLISH_PREFIX+clientMQTT.getClientId().toString())){//Notification for SSAP response messages
							//gets the message payload (SSAPMessage)
							String payload = new String(message.getPayload());
							
							payload= clearJsonMessage(payload);
							
							SSAPMessage ssapMessage = SSAPMessage.fromJsonToSSAPMessage(payload);
							//Si el mensaje es un JOIN recupera el SessionKey
							try{
								SSAPMessageTypes messageType=ssapMessage.getMessageType();
								if (messageType!=null && ssapMessage.getMessageType().equals(SSAPMessageTypes.JOIN)) {
									String sKey = SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody()).getData();
									sessionKey = sKey;
									if( SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody()).isOk()&&
											sKey!=null) {
										joined=true;
									}
								}else if (messageType!=null && ssapMessage.getMessageType().equals(SSAPMessageTypes.LEAVE) &&
										SSAPBodyReturnMessage.fromJsonToSSAPBodyReturnMessage(ssapMessage.getBody()).isOk()){
									
									joined=false;
								}	
							
								//Notifies the reception to unlock the synchronous waiting
							}catch(Exception e){
								log.error("Error procesando mensaje Recibido desde SIB: ", e);
							}
							if(responseCallback!=null){
								responseCallback.handle(payload);
							}
							
							
						}else if(messageTopic.equals(TOPIC_SUBSCRIBE_INDICATION_PREFIX+clientMQTT.getClientId().toString())){//Notification for ssap indication message
							//gets the message payload (SSAPMessage)
							String payload = new String(message.getPayload());
							
							payload= clearJsonMessage(payload);
							Collection<IndicationTask> tasks = new ArrayList<IndicationTask>();
							
							SSAPMessage ssapMessage = SSAPMessage.fromJsonToSSAPMessage(payload);
							if(ssapMessage.getMessageType() == SSAPMessageTypes.INDICATION){
								
								String messageId=ssapMessage.getMessageId();
								if(messageId!=null){
									//Notifica a los listener de las suscripciones hechas manualmente
					    			for (Iterator<Listener4SIBIndicationNotifications> iterator = subscriptionListeners.iterator(); iterator.hasNext();) {
										Listener4SIBIndicationNotifications listener = iterator.next();
										tasks.add(new IndicationTask(listener, messageId, ssapMessage));
									}
					    			
					    			//Notifica a los listener de las autosuscripciones
					    			if(messageId.equals(baseCommandRequestSubscriptionId) && listener4BaseCommandRequestNotifications!=null){
					    				tasks.add(new IndicationTask(listener4BaseCommandRequestNotifications, messageId, ssapMessage));
					    			}else if(messageId.equals(statusControlRequestSubscriptionId) && listener4StatusControlRequestNotifications!=null){
					    				tasks.add(new IndicationTask(listener4StatusControlRequestNotifications, messageId, ssapMessage));
					    			}
					    			KpMQTTClient.this.executeIndicationTasks(tasks);
					    		}
							}
							
						}
					}
				} catch (Exception e) {
					log.error("Error receiving message from SIB: " + e.getMessage(), e);
					if(responseCallback!=null){
						responseCallback.handle("");
					}
				}
			}
		}
	}
	
	
	private String clearJsonMessage(String payload){
		if(payload.startsWith("{") &&
				payload.endsWith("}") && 
				payload.contains("direction") && 
				payload.contains("sessionKey")){//mensaje no cifrado con XXTEA
			
			
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

	@Override
	public void messageReceived(byte[] message) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the MQTT clientID for this KP
	 * 
	 * @return
	 * @throws MQTTClientNotConfiguredException
	 */
	public String getClientId() throws MQTTClientNotConfiguredException {
		if (this.clientMQTT == null) {
			throw new MQTTClientNotConfiguredException("MQTT client is not configured yet. To configure MQTT client connect it to a MQTT server");
		} else {
			return this.clientMQTT.getClientId().toString();
		}
	}

	/**
	 * Internal class to receive the synchronous notifications for SSAP messages
	 * 
	 * @author jfgpimpollo
	 *
	 */
	class Callback {

		private final CountDownLatch latch = new CountDownLatch(1);

		private String response;

        String get() {
          try {
            latch.await(ssapResponseTimeout, TimeUnit.MILLISECONDS);
          } catch (InterruptedException e) {
        	 log.error("Error esperando respuesta desde SIB", e);
            throw new RuntimeException(e);
          }
          if ( response == null ) {
        	  if(KpMQTTClient.this.subscriptionThread.isStoped()){
        		  log.error("Se ha perdido la conexión física con Sofia2");
        		  logNetIsAvailable();
        		  throw new DisconnectFromSibException("Se ha perdido la conexión física con Sofia2");
        	  }else{
        		  log.error( "ssap mqtt response timeout for "  + ssapResponseTimeout + "ms"  );
        		  logNetIsAvailable();
        		  throw new SSAPResponseTimeoutException( "Timeout: " + ssapResponseTimeout + " waiting for SSAP Response" );
        	  }
          }
          return response;
        }

		void handle(String response) {
			this.response = response;
			latch.countDown();
		}
	}
	
	
	private void logNetIsAvailable() {  
//		 HttpURLConnection conn=null;
//		 try {                                                                                                                                                                                                                                 
//		        final URL url = new URL("http://www.google.es");                                                                                                                                                                                 
//		        conn =  (HttpURLConnection) url.openConnection();                                                                                                                                                                          
//		        conn.connect();
//		        if(HttpURLConnection.HTTP_OK == conn.getResponseCode()){
//		        	log.info("Hay acceso a Internet");
//		        }else{
//		        	log.info("No hay acceso a Internet");
//		        }
//		        
//		    } catch (MalformedURLException e) {                                                                                                                                                                                                   
//		    	log.info("Url de chequeo mal formada");                                                                                                                                                                                                    
//		    } catch (IOException e) {                                                                                                                                                                                                             
//		    	log.info("No hay acceso a Internet");                                                                                                                                                                                          
//		    }finally{
//		    	if(conn!=null){
//		    		conn.disconnect();
//		    	}
//		    }
	}  

}
