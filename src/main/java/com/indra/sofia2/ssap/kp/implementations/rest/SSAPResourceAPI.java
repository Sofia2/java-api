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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.indra.sofia2.ssap.kp.implementations.rest.exception.ResponseMapperException;
import com.indra.sofia2.ssap.kp.implementations.rest.resource.SSAPResource;

public class SSAPResourceAPI implements ISSAPResourceAPI {

	private ISSAPResourceAPI api = null;

	public SSAPResourceAPI(String serviceURL) {

		List<MessageBodyReader<?>> providers = new ArrayList<MessageBodyReader<?>>();

		providers.add(new JacksonJsonProvider());
		api = JAXRSClientFactory.create(serviceURL, ISSAPResourceAPI.class, providers);
	}

	public SSAPResourceAPI(String serviceURL, String server, Integer port, String user, String pass) {

		List<MessageBodyReader<?>> providers = new ArrayList<MessageBodyReader<?>>();

		providers.add(new JacksonJsonProvider());
		api = JAXRSClientFactory.create(serviceURL, ISSAPResourceAPI.class, providers);

		HTTPConduit conduit = WebClient.getConfig(api).getHttpConduit();
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setProxyServer(server);
		policy.setProxyServerPort(port);
		if (user != null && user != "" && pass != null && pass != "") {
			conduit.getProxyAuthorization().setUserName(user);
			conduit.getProxyAuthorization().setPassword(pass);
		}
		conduit.setClient(policy);
	}

	@Override
	public Response delete(SSAPResource ssap) {
		String data = ssap.getData();
		String sessionkey = ssap.getSessionKey();
		String ontology = ssap.getOntology();

		// El cliente de CXF ignora el cuerpo de la petición si se trata de un
		// DELETE por lo que invocamos a través de path param

		String objectId = null;
		if (data != null && !data.trim().equals("")) {
			Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				map = mapper.readValue(data, new TypeReference<HashMap<String, Map<String, String>>>() {
				});
				objectId = map.get("_id").get("$oid");
			} catch (Exception e) {

			}
		}

		if (objectId != null) {
			return api.deleteOid(objectId, sessionkey, ontology);
		} else {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Bad Request: could not get ObjectId from body Object").build();
		}
	}

	@Override
	public Response query(String sessionKey, String ontology, String query, String queryArguments, String queryType) {

		return api.query(sessionKey, ontology, query, queryArguments, queryType);
	}

	@Override
	public Response insert(SSAPResource ssap) {
		return api.insert(ssap);
	}

	@Override
	public Response update(SSAPResource ssap) {
		return api.update(ssap);
	}

	@Override
	public Response getConfig(String kp, String instanciakp, String token, String assetService,
			String assetServiceParam) {
		return api.getConfig(kp, instanciakp, token, assetService, assetServiceParam);
	}

	@Override
	public Response deleteOid(String oid, String sessionKey, String ontology) {
		return api.deleteOid(oid, sessionKey, ontology);
	}

	@Override
	public Response query(String oid, String sessionKey, String ontology) {
		return api.query(oid, sessionKey, ontology);
	}

	@Override
	public Response subscribe(String sessionKey, String ontology, String query, int msRefresh, String queryArguments,
			String queryType, String endpoint) {
		return api.subscribe(sessionKey, ontology, query, msRefresh, queryArguments, queryType, endpoint);
	}

	@Override
	public Response unsubscribe(String sessionKey, String subscriptionId) {
		return api.unsubscribe(sessionKey, subscriptionId);
	}

	@Override
	public SSAPResource responseAsSsap(Response resp) throws ResponseMapperException {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue((InputStream) resp.getEntity(), SSAPResource.class);
		} catch (JsonParseException e) {
			throw new ResponseMapperException(e);
		} catch (JsonMappingException e) {
			throw new ResponseMapperException(e);
		} catch (IOException e) {
			throw new ResponseMapperException(e);
		}
	}
}
