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
package com.indra.sofia2.ssap.kp.implementations.rest.response;

import java.io.Serializable;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.indra.sofia2.ssap.ssap.SSAPErrorCode;

@ApiObject(name = "CommonResponse", show=false)
public class CommonResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5996442695228247795L;
	
	@ApiObjectField(description = "The operation sucesfull")
	protected boolean ok;
	@ApiObjectField(description = "The error message")
    protected String error;
	@ApiObjectField(description = "The error code")
	protected SSAPErrorCode errorCode;

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
}
