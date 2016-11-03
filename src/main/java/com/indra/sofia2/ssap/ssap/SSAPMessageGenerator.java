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
package com.indra.sofia2.ssap.ssap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.indra.sofia2.ssap.kp.exceptions.NotJoinedException;
import com.indra.sofia2.ssap.kp.exceptions.SQLSentenceNotAllowedForThisOperationException;
import com.indra.sofia2.ssap.ssap.binary.BinarySizeException;
import com.indra.sofia2.ssap.ssap.binary.Encoding;
import com.indra.sofia2.ssap.ssap.binary.Mime;
import com.indra.sofia2.ssap.ssap.binary.Storage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyConfigMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinTokenMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyJoinUserAndPasswordMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyOperationMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyQueryWithParamMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodySubscribeMessage;
import com.indra.sofia2.ssap.ssap.body.SSAPBodyUnsubscribeMessage;
import com.indra.sofia2.ssap.ssap.body.binary.SSAPBinaryMessage;


public class SSAPMessageGenerator {

	private Map<String, SSAPBinaryMessage> binary = new HashMap<String, SSAPBinaryMessage>();
	private Map<String, Long> binarySize = new HashMap<String, Long>();
	
	private static SSAPMessageGenerator me = new SSAPMessageGenerator();

	public static SSAPMessageGenerator getInstance() {
		return me;
	}

	/**
	 * Constructor
	 */
	public SSAPMessageGenerator() {
	}
	
	private void validateSize() throws BinarySizeException{
		long size = 0;
		for (String key : binarySize.keySet()){
			size=size+binarySize.get(key);
		}
		if (size>1000000){
			throw new BinarySizeException("El tamaño de los ficheros almacenados para enviar con la ontología es de "+size+" bytes");
		}
	}

	/**
	 * Se añade un fichero con el encoding por defecto Base64 al map de ficheros
	 * @param fieldName
	 * @param binary
	 * @param mime
	 */
	public void addBinary(String fieldName, File binary, Mime mime) throws BinarySizeException {
		SSAPBinaryMessage binaryField = new SSAPBinaryMessage(binary, Storage.SERIALIZED, Encoding.Base64, mime);
		this.binary.put(fieldName, binaryField);
		this.binarySize.put(fieldName, binary.length());
		validateSize();
	}
	
	/**
	 * Se añade un fichero con el encoding por defecto Base64 al map de ficheros
	 * @param fieldName
	 * @param binary
	 * @param mime
	 */
	public void addBinary(String fieldName, File binary, Storage storageArea, Encoding encoding, Mime mime) throws BinarySizeException{
		SSAPBinaryMessage binaryField = new SSAPBinaryMessage(binary, storageArea, encoding, mime);
		this.binary.put(fieldName, binaryField);
		validateSize();
	}
	
	/**
	 * Se añade un fichero con el encoding por defecto Base64 al map de ficheros
	 * @param fieldName
	 * @param binary
	 * @param mime
	 */
	public void removeBinary(String fieldName){
		this.binary.remove(fieldName);
		this.binarySize.remove(fieldName);
	}
	
	/**
	 * Método que limpia la lista de ficheros binarios.
	 */
	public void cleanBinary(){
		for (String key : binary.keySet()){
			this.binary.remove(key);
			this.binarySize.remove(key);
		}
	}
	
	/**
	 * Metodo que genera la estructura JSON con los tipo de datos binary.
	 * @return
	 */
	public String generateJSONBinary(){
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iterator = binary.keySet().iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			buffer.append("\"");
			buffer.append(key);
			buffer.append("\":");
			buffer.append(binary.get(key).toJson());
			if (iterator.hasNext()){
				buffer.append(",");
			}	
		}
		return buffer.toString();
	}
	
	/**
	 * Metodo que genera una estructura Clave Valor con todos los tipo de datos binary contenidos en un JSON.
	 * @return
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Map<String, SSAPBinaryMessage> getBinary(LinkedHashMap JSON){
		Map<String, SSAPBinaryMessage> binary = new HashMap<String, SSAPBinaryMessage>();
		try {
			subAnalize(JSON, binary);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return binary;
	}
	
	@SuppressWarnings({ "unchecked" })
	private SSAPBinaryMessage subAnalize(Map<String,Object> JSONSubStructure, Map<String, SSAPBinaryMessage> binary) {
		for (Entry<String, Object> jsonElement : JSONSubStructure.entrySet()){
			if (jsonElement.getValue() instanceof LinkedHashMap){
				if (jsonElement.getKey().equals("media")){
					StringBuffer json = new StringBuffer("{\"data\":\"");
					json.append(JSONSubStructure.get("data"));
					json.append("\",\"media\":{");
					Map<String,Object> media = (Map<String,Object>)JSONSubStructure.get("media");
					json.append("\"binaryEncoding\":\"");
					json.append(media.get("binaryEncoding"));
					json.append("\",\"mime\":\"");
					json.append(media.get("mime"));
					json.append("\",\"name\":\"");
					json.append(media.get("name"));
					json.append("\"}}");
					try {
						return SSAPBinaryMessage.fromJsonToSSAPBinaryMessage(json.toString());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}else{
					SSAPBinaryMessage binaryMessage = subAnalize(((Map<String,Object>)jsonElement.getValue()), binary);
					if (binaryMessage!=null){
						binary.put(jsonElement.getKey(), binaryMessage);
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Genera un mensaje JOIN con autenticación basada en usuario y password
	 * @param usuario
	 * @param password
	 * @param instance
	 * @return
	 */
	public SSAPMessage generateJoinMessage(String usuario, String password, String instance) {
		SSAPMessage mensaje = new SSAPMessage();
		SSAPBodyJoinUserAndPasswordMessage body = new SSAPBodyJoinUserAndPasswordMessage();
		body.setPassword(password);
		body.setUser(usuario);
		body.setInstance(instance);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.JOIN);
		return mensaje;
	}
	
	/**
	 * Genera un mensaje JOIN para renovación de sessionkey
	 * @param usuario
	 * @param password
	 * @param instance
	 * @param sessionkey
	 * @return
	 */
	public SSAPMessage generateJoinMessage(String usuario, String password, String instance, String sessionkey) {
		SSAPMessage mensaje = new SSAPMessage();
		SSAPBodyJoinUserAndPasswordMessage body = new SSAPBodyJoinUserAndPasswordMessage();
		body.setPassword(password);
		body.setUser(usuario);
		body.setInstance(instance);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.JOIN);
		mensaje.setSessionKey(sessionkey);
		return mensaje;
	}

	/**
	 * Genera un mensaje JOIN con autenticación basada en token
	 * @param token
	 * @param instance
	 * @return
	 */
	public SSAPMessage generateJoinByTokenMessage(String token, String instance){
		SSAPMessage mensaje = new SSAPMessage();
		SSAPBodyJoinTokenMessage body = new SSAPBodyJoinTokenMessage();
		body.setToken(token);
		body.setInstance(instance);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.JOIN);
		return mensaje;
	}
	
	
	/**
	 * Genera un mensaje JOIN con autenticación basada en token para renovación de sessionkey
	 * @param token
	 * @param instance
	 * @return
	 */
	public SSAPMessage generateJoinByTokenMessage(String token, String instance, String sessionkey){
		SSAPMessage mensaje = new SSAPMessage();
		SSAPBodyJoinTokenMessage body = new SSAPBodyJoinTokenMessage();
		body.setToken(token);
		body.setInstance(instance);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.JOIN);
		mensaje.setSessionKey(sessionkey);
		return mensaje;
	}

	/**
	 * genera un mensaje LEAVE para cerrar la sesión 
	 * @param sessionKey
	 * @return
	 */
	public SSAPMessage generateLeaveMessage(String sessionKey) {
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.LEAVE);
		return mensaje;
	}

	/**
	 * genera un mensaje INSERT de tipo nativo
	 * @param sessionKey
	 * @param ontologia
	 * @param datos
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateInsertMessage(String sessionKey, String ontologia, String datos) {
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		body.setData(datos);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.INSERT);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * genera un mensaje de INSERT del tipo indicado en el argumento queryType
	 * @param sessionKey
	 * @param ontologia
	 * @param datos
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateInsertMessage(String sessionKey, String ontologia, String datos, SSAPQueryType queryType) throws SQLSentenceNotAllowedForThisOperationException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		if(isInsert(datos, queryType)){
			if(queryType==SSAPQueryType.SQLLIKE){
				body.setQuery(datos);
			}else{
				body.setData(datos);
			}
		}else{
			throw new SQLSentenceNotAllowedForThisOperationException (new Exception("ERROR - Expected insert values"));
		}
		body.setQueryType(queryType);
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setBody(body.toJson());
		mensaje.setMessageType(SSAPMessageTypes.INSERT);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	private boolean isInsert(String datos, SSAPQueryType queryType){
		if(queryType != null && datos.length()>0){
			switch(queryType){
			case SQLLIKE:			
			case NATIVE:
				return datos.toUpperCase().contains("INSERT");
			default:
				return false;
			}
		}else {
			return datos.length() > 0;
		}
	}

	/**
	 * Genera un mensaje UPDATE de tipo nativo
	 * @param sessionKey
	 * @param ontologia
	 * @param datos
	 * @param query
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateUpdateMessage(String sessionKey, String ontologia, String datos, String query)throws NotJoinedException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		body.setData(datos);
		body.setQuery(query);
		body.setQueryType(SSAPQueryType.NATIVE);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.UPDATE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}



	/**
	 * Genera un mensaje UPDATE del tipo indicado en el argumento queryType
	 * @param sessionKey
	 * @param ontologia
	 * @param datos
	 * @param query
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateUpdateMessage(String sessionKey, String ontologia, String datos, String query, SSAPQueryType queryType)throws SQLSentenceNotAllowedForThisOperationException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		
		if ( isUpdate(query, queryType)){
			body.setQuery(query);
		}else{
			throw new SQLSentenceNotAllowedForThisOperationException(new Exception("ERROR - Expected update query"));
		}
		body.setData(datos);
		body.setQueryType(queryType);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.UPDATE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * Genera un mensaje REMOVE de tipo nativo
	 * @param sessionKey
	 * @param ontologia
	 * @param query
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateRemoveMessage(String sessionKey, String ontologia, String query)throws NotJoinedException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		body.setQuery(query);
		body.setQueryType(SSAPQueryType.NATIVE);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.DELETE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * Genera un mensaje REMOVE del tipo indicado en el argumento queryType
	 * @param sessionKey
	 * @param ontologia
	 * @param query
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateRemoveMessage(String sessionKey, String ontologia, String query, SSAPQueryType queryType)throws SQLSentenceNotAllowedForThisOperationException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		if(isRemove(query, queryType)){
			body.setQuery(query);
		}else{
			throw new SQLSentenceNotAllowedForThisOperationException(new Exception("Error - statement no expected"));
		}
		body.setQueryType(queryType);
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setBody(body.toJson());
		mensaje.setMessageType(SSAPMessageTypes.DELETE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}


	/**
	 * Genera un mensaje QUERY de tipo nativo
	 * @param sessionKey
	 * @param idQuery
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateQueryMessage(String sessionKey, String ontologia, String query) throws NotJoinedException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		body.setQuery(query);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.QUERY);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * Genera un mensaje QUERY del tipo pasado por parámetros
	 * @param sessionKey
	 * @param idQuery
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateQueryMessage(String sessionKey, String ontologia, String query, SSAPQueryType queryType) throws SQLSentenceNotAllowedForThisOperationException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		if(isQuery(query, queryType)){
			body.setQuery(query);
		}else{
			throw new SQLSentenceNotAllowedForThisOperationException(new Exception("ERROR - statement no expected"));
		}
		body.setQueryType(queryType);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.QUERY);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * Genera un mensaje QUERY para queries predefinidas en el SIB
	 * @param sessionKey
	 * @param idQuery
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateQueryMessage (String sessionKey, String idQuery) throws NotJoinedException{
		SSAPMessage message = new SSAPMessage();
		message.setSessionKey(sessionKey);
		SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
		body.setQuery(idQuery);
		body.setQueryType(SSAPQueryType.SIB_DEFINED);
		message.setBody(body.toJson());
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.QUERY);
		return message;
	}

	/**
	 * Genera mensaje QUERY para queries predefinidas en el SIB y permite pasar parametros a la query
	 * @param sessionKey
	 * @param idQuery
	 * @param params
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateQueryMessageWithParam (String sessionKey, String idQuery, Map<String,String> params) throws NotJoinedException{
		SSAPMessage message = new SSAPMessage();
		message.setSessionKey(sessionKey);
		//Segun el tipo de query que llege se creara un body diferente
		//para el tipo SIB_DIFINED pueden venir parametros y es necesario utilizar SSAPBodyQueryWithParamMessage en lugar de SSAPQueryMessage
		if(idQuery.length()>0 && params != null){
			SSAPBodyQueryWithParamMessage body = new SSAPBodyQueryWithParamMessage();
			if(isQuery(idQuery, SSAPQueryType.SIB_DEFINED)){
				body.setQuery(idQuery);
			}else{
				throw new SQLSentenceNotAllowedForThisOperationException(new Exception("ERROR - statement no expected"));
			}
			body.setQueryType(SSAPQueryType.SIB_DEFINED);
			body.setQueryParams(params);
			message.setBody(body.toJson());
		}else{
			SSAPBodyOperationMessage body = new SSAPBodyOperationMessage();
			if(isQuery(idQuery, SSAPQueryType.SIB_DEFINED)){
				body.setQuery(idQuery);
			}else{
				throw new SQLSentenceNotAllowedForThisOperationException(new Exception("ERROR - statement no expected"));
			}
			body.setQueryType(SSAPQueryType.SIB_DEFINED);
			message.setBody(body.toJson());

		}
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.QUERY);
		return message;
	}

	/**
	 * Genera un mensaje SUBSCRIBE del tipo nativo
	 * @param sessionKey
	 * @param idQuery
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateSubscribeMessage(String sessionKey, String ontologia, int msRefresh, String query)throws NotJoinedException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodySubscribeMessage body = new SSAPBodySubscribeMessage();
		body.setQuery(query);
		body.setMsRefresh(msRefresh);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.SUBSCRIBE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}

	/**
	 * Genera un mensaje SUBSCRIBE de tipo pasado por parametros
	 * @param sessionKey
	 * @param idQuery
	 * @param queryType
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateSubscribeMessage(String sessionKey, String ontologia, int msRefresh, String query, SSAPQueryType queryType)throws SQLSentenceNotAllowedForThisOperationException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodySubscribeMessage body = new SSAPBodySubscribeMessage();


		body.setQueryType(queryType);

//		if(isQuery(query, queryType)){
//			body.setQuery(query);
//		}else{
//			throw new SQLSentenceNotAllowedForThisOperationException(new Exception("ERROR - statement no expected"));
//		}

		body.setQuery(query);

		body.setMsRefresh(msRefresh);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.SUBSCRIBE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}




	/**
	 * Genera un mensaje UNSUBSCRIBE
	 * @param sessionKey
	 * @param ontologia
	 * @param idSuscripcion
	 * @return
	 * @throws NotJoinedException
	 */
	public SSAPMessage generateUnsubscribeMessage(String sessionKey, String ontologia, String idSuscripcion)throws NotJoinedException{
		SSAPMessage mensaje = new SSAPMessage();
		mensaje.setSessionKey(sessionKey);
		SSAPBodyUnsubscribeMessage body = new SSAPBodyUnsubscribeMessage();
		body.setIdSuscripcion(idSuscripcion);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.UNSUBSCRIBE);
		mensaje.setOntology(ontologia);
		return mensaje;
	}


	private boolean isUpdate(String query, SSAPQueryType queryType){
		if(queryType != null && query.length()>0){
			switch (queryType){
			case SQLLIKE:
			case NATIVE:
				return query.toUpperCase().contains("UPDATE");
			default:
				return false;
			}
		}else{
			return query.length() > 0;
		}
	}

	private boolean isRemove(String query, SSAPQueryType queryType){
		if(queryType !=null && query.length()>0){
			switch(queryType){
			case SQLLIKE:
				return query.toUpperCase().contains("DELETE ");
			case NATIVE:
				return query.toUpperCase().contains("REMOVE");
			default:
				return false;
			}
		}else{
			return query.length() > 0;
		}
	}

	private boolean isQuery(String query, SSAPQueryType queryType){
		if(queryType !=null && query.length()>0){
			switch(queryType){
			case SQLLIKE:
			case BDH:
				return query.toUpperCase().contains("SELECT ")||query.toUpperCase().contains("INSERT ")||query.toUpperCase().contains("UPDATE ")||query.toUpperCase().contains("DELETE ");
			case NATIVE:
					return query.toUpperCase().contains("FIND") || query.toUpperCase().startsWith("DB.") || query.toUpperCase().contains("SELECT ") ||
							query.toUpperCase().contains("INSERT ") || query.toUpperCase().contains("UPDATE ") || query.toUpperCase().contains("DELETE ");
					
			case SIB_DEFINED:
			case CEP:
				return true;
			case BDC:
				return query.toUpperCase().contains("SELECT ");
			default:
				return false;
			}
		}else{
			return query.length() > 0;
		}
	}
	
	/**
	 * 
	 * @param kp
	 * @param instanciaKp
	 * @param token
	 * @return
	 */
	public SSAPMessage generateGetConfigMessage(String kp, String instanciaKp, String token, String assetService, HashMap<String,String> assetServiceParam ){
		SSAPMessage mensaje = new SSAPMessage();
		SSAPBodyConfigMessage body = new SSAPBodyConfigMessage();
		body.setInstanciaKp(instanciaKp);
		body.setKp(kp);
		body.setToken(token);
		body.setAssetService(assetService);
		body.setAssetServiceParam(assetServiceParam);
		mensaje.setBody(body.toJson());
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.CONFIG);
		return mensaje;
	}
	
	
	
	/**
	 * Genera un mensaje de tipo BULK
	 * @param sessionKey
	 * @param ontologia
	 * @param query
	 * @return
	 */
	public SSAPBulkMessage generateBulkMessage(String sessionKey, String ontologia){
		SSAPBulkMessage mensaje = new SSAPBulkMessage();
		mensaje.setSessionKey(sessionKey);
		mensaje.setDirection(SSAPMessageDirection.REQUEST);
		mensaje.setMessageType(SSAPMessageTypes.BULK);
		mensaje.setOntology(ontologia);
		
		return mensaje;
	}

}
