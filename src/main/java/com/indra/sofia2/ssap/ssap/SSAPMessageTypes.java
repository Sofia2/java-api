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

public enum SSAPMessageTypes {
	/*
	 * Transacciones para logado y logout en SIB
	 */
	JOIN,
	LEAVE,
	/*
	 * Transacciones operativas
	 */
	INSERT,
	UPDATE,
	DELETE,
	
	/*
	 * Transacciones de consulta
	 */
	QUERY,
	
	/*
	 * Transacciones para subscripciones
	 */
	SUBSCRIBE,
	UNSUBSCRIBE,
	INDICATION,
	BULK,
	
	/*
	 * Despliegue
	 */
	CONFIG,
	
	/*
	 * Index
	 */
	CREATE,
	DELETEINDEX,
	GETINDEXES;
	
	public String value() {
        return name();
    }

    public static SSAPMessageTypes fromValue(String v) {
        return valueOf(v);
    }
	
}
