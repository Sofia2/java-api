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

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.sofia2.ssap.ssap.SSAPErrorCode;

public class SSAPBodyReturnMessage {

	/*
	 * Datos que intervienen en la operación
	 */
	private String data;

	/*
	 * Indica si la ejecucion fue satisfactoria
	 */
	private boolean ok = true;
	/*
	 * Indica el error que produjo una ejecución no satosfactoria
	 */
	private String error;
	/*
	 * Indica el error normalizado producido
	 */
	private SSAPErrorCode errorCode;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public SSAPErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(SSAPErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SSAPBodyReturnMessage fromJsonToSSAPBodyReturnMessage(String json) throws IOException {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.enableDefaultTyping();
		return objMapper.readValue(json, SSAPBodyReturnMessage.class);
	}
}
