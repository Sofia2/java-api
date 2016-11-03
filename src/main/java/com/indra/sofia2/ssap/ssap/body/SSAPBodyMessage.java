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
package com.indra.sofia2.ssap.ssap.body;

public abstract class SSAPBodyMessage {

	/**
	 * Añade {} en el String que representa el JSON si no tiene el { al inicio
	 * del documento
	 * 
	 * @param query
	 * @return
	 */
	protected String prepareQuotes(String query) {
		String myQuery = query;
		if (myQuery == null) {
			myQuery = "";
		}
		StringBuffer cadena = new StringBuffer(myQuery);
		if (!myQuery.startsWith("{")) {
			cadena.insert(0, "{");
			cadena.append("}");
		}
		return cadena.toString();
	}

}
