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
package com.indra.sofia2.ssap.ssap;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.kp.exceptions.SSAPMessageDeserializationError;

public class SSAPMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Identificador unico de una peticion
	 */
	protected String messageId;

	/*
	 * Identificador de la session con un SIB
	 */
	protected String sessionKey;

	/*
	 * Identificador de la ontologia que referencia el mensaje
	 */
	protected String ontology;

	/*
	 * Direccion de sentido del mensaje
	 */
	protected SSAPMessageDirection direction;

	/*
	 * Tipo de mensaje
	 */
	protected SSAPMessageTypes messageType;
	/*
	 * Cuerpo del Mensaje
	 */
	protected String body;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public SSAPMessageDirection getDirection() {
		return direction;
	}

	public void setDirection(SSAPMessageDirection direction) {
		this.direction = direction;
	}

	public SSAPMessageTypes getMessageType() {
		return messageType;
	}

	public void setMessageType(SSAPMessageTypes messageType) {
		this.messageType = messageType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPMessage fromJsonToSSAPMessage(String json) {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.enableDefaultTyping();
		try {
			return objMapper.readValue(json, SSAPMessage.class);
		} catch (IOException e) {
			throw new SSAPMessageDeserializationError(e);
		}
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
