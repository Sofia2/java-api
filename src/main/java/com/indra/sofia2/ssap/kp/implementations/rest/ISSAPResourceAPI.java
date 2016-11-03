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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.indra.sofia2.ssap.kp.implementations.rest.exception.ResponseMapperException;
import com.indra.sofia2.ssap.kp.implementations.rest.resource.SSAPResource;

public interface ISSAPResourceAPI {

    Response delete(SSAPResource ssap);

    Response query(@QueryParam("$sessionKey") String $sessionKey, @QueryParam("$ontology") String $ontology, @QueryParam("$query") @DefaultValue("null") String $query, @QueryParam("$queryArguments") @DefaultValue("null") String $queryArguments, 
                @QueryParam("$queryType") @DefaultValue("SQLLIKE") String $queryType);

    
	Response subscribe(@QueryParam("$sessionKey") String sessionKey,
			@QueryParam("$ontology") String ontology,
			@QueryParam("$query") String query,
			@QueryParam("$msRefresh") int msRefresh,
			@QueryParam("$queryArguments") String queryArguments,
			@QueryParam("$queryType") String queryType,
			@QueryParam("$endpoint") String endpoint);
	
	
	Response unsubscribe(@QueryParam("$sessionKey") String sessionKey,
			@QueryParam("$subscriptionId") String subscriptionId);
	
    
    Response insert(SSAPResource ssap);

    Response update(SSAPResource ssap);

    Response getConfig(@QueryParam("$kp") String $kp, @QueryParam("$instanciakp") String $instanciakp, @QueryParam("$token") @DefaultValue("null") String $token, @QueryParam("$assetService") @DefaultValue("null") String $assetService, @QueryParam("$assetServiceParam") @DefaultValue("null") String $assetServiceParam);

    Response deleteOid(@PathParam("oid") String oid, @QueryParam("$sessionKey") String $sessionKey, @QueryParam("$ontology") String $ontology);

    Response query(@PathParam("oid") String oid, @QueryParam("$sessionKey") String $sessionKey, @QueryParam("$ontology") String $ontology);
    
    SSAPResource responseAsSsap(Response o)throws ResponseMapperException;

}