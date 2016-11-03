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
package com.indra.sofia2.ssap.kp.implementations.rest;

import javax.ws.rs.core.Response;

import com.indra.sofia2.ssap.kp.implementations.rest.exception.ResponseMapperException;
import com.indra.sofia2.ssap.kp.implementations.rest.resource.SSAPResource;

public interface ISSAPResourceAPI {

	Response delete(SSAPResource ssap);

	Response query(String sessionKey, String ontology, String query, String queryArguments, String queryType);

	Response subscribe(String sessionKey, String ontology, String query, int msRefresh, String queryArguments,
			String queryType, String endpoint);

	Response unsubscribe(String sessionKey, String subscriptionId);

	Response insert(SSAPResource ssap);

	Response update(SSAPResource ssap);

	Response getConfig(String kp, String instanciakp, String token, String assetService, String assetServiceParam);

	Response deleteOid(String oid, String sessionKey, String ontology);

	Response query(String oid, String sessionKey, String ontology);

	SSAPResource responseAsSsap(Response o) throws ResponseMapperException;

}