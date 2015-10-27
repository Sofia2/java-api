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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="ssapmessage")
@XmlType(name="ssapmessage") 
public class SSAPMessage implements Serializable{

	/**
	 * 
	 */
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
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class").serialize(this);
    }
    
    public static SSAPMessage fromJsonToSSAPMessage(String json) {
        return new JSONDeserializer<SSAPMessage>().use(null, SSAPMessage.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<SSAPMessage> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<SSAPMessage> collection, String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<SSAPMessage> fromJsonArrayToSSAPMessages(String json) {
        return new JSONDeserializer<List<SSAPMessage>>().use(null, ArrayList.class).use("values", SSAPMessage.class).deserialize(json);
    }
    
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    
}
