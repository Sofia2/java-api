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
package com.indra.sofia2.ssap.kp;

import com.indra.sofia2.ssap.kp.config.ConnectionConfig;
import com.indra.sofia2.ssap.kp.exceptions.ConnectionToSibException;
import com.indra.sofia2.ssap.ssap.SSAPMessage;

/**
 * Interfaz Kp,
 * Existiran implementaciones para diferentes protocolos
 * @author lmgracia
 *
 */
public interface Kp {
	
	/**
	 * indica si esta conectado con SIB
	 * @deprecated Usar isConnectionEstablished()
	 * en lugar de este método.
	 */
	@Deprecated
	boolean isConnected();
	
	/**
	 * indica si la conexión física está establecida para los protocolos
	 * que la utilicen.
	 */
	boolean isConnectionEstablished();
	
	/**
	 * nos da el SessionKey si esta conectado con un SIB 
	 * devuelve null si no esta conectado con SIB
	 */
	String getSessionKey();
	
	/**
	 * conecta con el SIB
	 * aun no se ha enviado mensaje JOIN, sirve para conectar
	 * si no hay conexion devuelve ConnectionToSibException con el error de la conexion
	 */
	void connect() throws ConnectionToSibException;
	
	/**
	 * Hace la desconexion del protocolo físico
	 */
	void disconnect();
	

	/**
	 * Parametros de configuracion para la conexion
	 */
	void setConnectionConfig(ConnectionConfig config);
	
	/**
	 * clave de cifrado xxtea
	 * @param cipherKey
	 */
	void setXxteaCipherKey(String cipherKey);
	
	/**
	 * envia al SIB cualquier mensaje
	 * @return
	 */
	SSAPMessage send(SSAPMessage msg) throws ConnectionToSibException;
	
	/**
	 * Envia al SIB un mensaje  cifrado con la clave
	 * @param msg
	 * @param cipherKey
	 * @return
	 * @throws ConnectionToSibException
	 */
	SSAPMessage sendCipher(SSAPMessage msg) throws ConnectionToSibException;
		
	/**
	 * Mecanismo para registrar un escuchador de notificaciones del SIB a suscripciones
	 * @param listener
	 */
	void addListener4SIBNotifications(Listener4SIBIndicationNotifications listener);

	/**
	 * Mecanismo para desregistrar un escuchador de notificaciones del SIB a suscripciones
	 * @param listener
	 */
	 void removeListener4SIBNotifications(Listener4SIBIndicationNotifications listener);
	 
	 /**
	  * Mecanismo para desregistrar todos los escuchadores de notificaciones del SIb a suscripciones
	  */
	 public void removeListener4SIBNotifications(); 
	 
		
	/**
	 * Method to register a listener to recevice raw messages notifications from SIB
	 * @param listener
	 */
	void addListener4SIBCommandMessageNotifications(Listener4SIBCommandMessageNotifications listener);

	/**
	 * Mecanismo para desregistrar un escuchador de notificaciones del SIB a suscripciones
	 * @param listener
	 */
	 void removeListener4SIBCommandMessageNotifications(Listener4SIBCommandMessageNotifications listener);
	 
	 
	 /**
	  * Method to register a listener to receive BaseCommandRequets from SIB
	  * @param listener
	  */
	void setListener4BaseCommandRequestNotifications(Listener4SIBIndicationNotifications listener);
	
	/**
	  * Method to register a listener to receive BaseCommandRequets from SIB
	  * @param listener
	  */
	void setListener4StatusControlRequestNotifications(Listener4SIBIndicationNotifications listener);

	/**
	 * Register connection Listeners to receive disconnection notificacions
	 * @param connectionListener
	 */
	void setConnectionListener(ConnectionListener connectionListener);
	

	/**
	 * Unregister connections Listeners
	 * @param connectionListener
	 */
	void removeConnectionListener();
}
