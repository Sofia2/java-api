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
package com.indra.sofia2.ssap.ssap.body;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


/**
 * Implementacion de JoinMessage
 * @author lmgracia
 *
 */
public class SSAPBodyJoinMessage extends SSAPBodyMessage {
	
	/*
	 * Identificador del dispositivo que se loga
	 */
	private String instance;

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	
	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class").serialize(this);
    }
    
    public static SSAPBodyJoinMessage fromJsonToSSAPBodyJoinMessage(String json) {
        return new JSONDeserializer<SSAPBodyJoinMessage>().use(null, SSAPBodyJoinMessage.class).deserialize(json);
    }
    
    public static Collection<SSAPBodyJoinMessage> fromJsonArrayToSSAPBodyJoinMessages(String json) {
        return new JSONDeserializer<List<SSAPBodyJoinMessage>>().use(null, ArrayList.class).use("values", SSAPBodyJoinMessage.class).deserialize(json);
    }
	
}
