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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.indra.sofia2.ssap.kp.exceptions.NotSupportedMessageTypeException;
import com.indra.sofia2.ssap.ssap.body.bulk.message.SSAPBodyBulkItem;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class SSAPBulkMessage extends SSAPMessage {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Correc: Instanciacion String
	public SSAPBulkMessage(){
		this.body="[]";
	}
	
	public SSAPBulkMessage addMessage(SSAPMessage ssapMessage) throws NotSupportedMessageTypeException{
		this.checkMessageType(ssapMessage.getMessageType());
		
		SSAPBodyBulkItem item=new SSAPBodyBulkItem();
		item.setType(ssapMessage.getMessageType());
		item.setBody(ssapMessage.getBody());
		item.setOntology(ssapMessage.getOntology());
		
		Collection<SSAPBodyBulkItem> lItems=SSAPBodyBulkItem.fromJsonArrayToSSAPBodyBulkItems(this.body);
		lItems.add(item);
		
		this.body=SSAPBodyBulkItem.toJsonArray(lItems);
		
		return this;
		
	}
	
	public void addMessage(List<SSAPMessage> ssapMessages) throws NotSupportedMessageTypeException{
		Collection<SSAPBodyBulkItem> lItems=SSAPBodyBulkItem.fromJsonArrayToSSAPBodyBulkItems(this.body);
		
		for(SSAPMessage ssapMessage:ssapMessages){
			this.checkMessageType(ssapMessage.getMessageType());
			
			SSAPBodyBulkItem item=new SSAPBodyBulkItem();
			item.setType(ssapMessage.getMessageType());
			item.setBody(ssapMessage.getBody());
			item.setOntology(ssapMessage.getOntology());
			lItems.add(item);
				
		}
		
		this.body=SSAPBodyBulkItem.toJsonArray(lItems);
	}
	
	private void checkMessageType(SSAPMessageTypes type) throws NotSupportedMessageTypeException{
		switch(type){
			case INSERT:
			case UPDATE:
			case DELETE:break;
			default: throw new NotSupportedMessageTypeException("Message type: "+type+" is not supported by SSAPBulkMessage");
		}
	}
	
	
	public String toJson() {
		return new JSONSerializer().exclude("lItems").exclude("*.class").serialize(this);
    }
    
    public String toJson(String[] fields) {
    	return new JSONSerializer().exclude("lItems").include(fields).exclude("*.class").serialize(this);
    }
    
    public static SSAPMessage fromJsonToSSAPBulkMessage(String json) {
        return new JSONDeserializer<SSAPMessage>().use(null, SSAPMessage.class).deserialize(json);
    }
    
    public static Collection<SSAPBulkMessage> fromJsonArrayToSSAPBulkMessages(String json) {
        return new JSONDeserializer<List<SSAPBulkMessage>>().use(null, ArrayList.class).use("values", SSAPBulkMessage.class).deserialize(json);
    }

}
