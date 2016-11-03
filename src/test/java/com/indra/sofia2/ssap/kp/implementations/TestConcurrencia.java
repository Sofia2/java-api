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

package com.indra.sofia2.ssap.kp.implementations;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indra.sofia2.ssap.kp.implementations.rest.SSAPResourceAPI;
import com.indra.sofia2.ssap.kp.implementations.rest.exception.ResponseMapperException;
import com.indra.sofia2.ssap.kp.implementations.rest.resource.SSAPResource;

public class TestConcurrencia implements Runnable{
	
	private static Logger log = LoggerFactory.getLogger(KpGatewayRestFuncional.class);
	
	private final static String SERVICE_URL="http://sofia2.com/sib/services/api_ssap/";
	
	private final static String TOKEN = "e5e8a005d0a248f1ad2cd60a821e6838";
	private final static String KP_INSTANCE = "KPTestTemperatura:KPTestTemperatura01";
	
	private final static String ONTOLOGY_NAME = "TestSensorTemperatura";
	private final static String ONTOLOGY_INSTANCE = "{ \"Sensor\": { \"geometry\": { \"coordinates\": [ 40.512967, -3.67495 ], \"type\": \"Point\" }, \"assetId\": \"S_Temperatura_00066\", \"measure\": 25, \"timestamp\": { \"$date\": \"2014-04-29T08:24:54.005Z\"}}}";
//	private final static String ONTOLOGY_UPDATE = "{ \"_id\":{\"$oid\": \"<ObjId>\"}, \"Sensor\": { \"geometry\": { \"coordinates\": [ 40.512967, -3.67495 ], \"type\": \"Point\" }, \"assetId\": \"S_Temperatura_00066\", \"measure\": 20, \"timestamp\": { \"$date\": \"2014-04-29T08:24:54.005Z\"}}}";
//	private final static String ONTOLOGY_QUERY_NATIVE_CRITERIA = "{\"Sensor.assetId\": \"S_Temperatura_00066\"}";
//	private final static String ONTOLOGY_QUERY_NATIVE_STATEMENT = "db.TestSensorTemperatura.find({\"Sensor.assetId\": \"S_Temperatura_00066\"})";
//	private final static String ONTOLOGY_QUERY_SQLLIKE = "select * from TestSensorTemperatura where Sensor.assetId = \"S_Temperatura_00066\"";
//	private final static String ONTOLOGY_INSERT_SQLLIKE = "insert into TestSensorTemperatura(geometry, assetId, measure, timestamp) values (\"{ 'coordinates': [ 40.512967, -3.67495 ], 'type': 'Point' }\", \"S_Temperatura_00066\", 15, \"{ '$date': '2014-04-29T08:24:54.005Z'}\")";
//	private final static String ONTOLOGY_DELETE = "{\"_id\":{\"$oid\":\"<ObjId>\"}}";
												  
	
	private SSAPResourceAPI api;
	
	public boolean leave;
	
	
	
	@Before
	public void setUpBeforeClass() throws Exception {
		this.api=new SSAPResourceAPI(SERVICE_URL);
	}
	
	
	@Test
	public void launchTest(){
		ExecutorService executor = Executors.newFixedThreadPool(50);
		
		for(int j=0;j<2;j++){
			for(int i=0;i<50;i++){
				TestConcurrencia t=new TestConcurrencia();
				if(Math.random()>0.5){
					t.leave=true;	
				}else{
					t.leave=false;
				}
				
				executor.execute(t);
			}
			
			System.out.println("############################################");
			System.out.println("Procesados 50 mensajes");
			System.out.println("############################################");
			
			try {
				Thread.sleep(10000);//Espera 30 sg
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("############################################");
		System.out.println("Termina de insertar");
		System.out.println("############################################");
		
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
		
		
		@Override
		public void run() {
			this.api=new SSAPResourceAPI(SERVICE_URL);
			//Genera un recurso SSAP con un JOIN
			SSAPResource ssapJoin=new SSAPResource();
			ssapJoin.setJoin(true);
			ssapJoin.setInstanceKP(KP_INSTANCE);
			ssapJoin.setToken(TOKEN);
			
			
			//Hace un POST del recurso, equivalente a una petici�n SSAP JOIN
			Response respJoin=this.api.insert(ssapJoin);
			
			String sessionkey=null;
			log.info("Codigo http retorno JOIN: "+respJoin.getStatus());
			try{
				sessionkey=this.api.responseAsSsap(respJoin).getSessionKey();
				log.info("Sessionkey:"+sessionkey);
				assertEquals(respJoin.getStatus(), 200);
				
			}catch(ResponseMapperException e){
				log.info(e.getMessage());
				assertEquals(false, true);
			}
			
			
			
			
			//Genera un recurso SSAP con un INSERT
			SSAPResource ssapInsert=new SSAPResource();
			ssapInsert.setData(ONTOLOGY_INSTANCE);
			ssapInsert.setSessionKey(sessionkey);
			ssapInsert.setOntology(ONTOLOGY_NAME);
					
			//Hace un POST del recurso, equivalente a una petici�n SSAP INSERT
			Response respInsert=this.api.insert(ssapInsert);
			
			log.info("Codigo http retorno INSERT: "+respInsert.getStatus());
			try{
				String data=this.api.responseAsSsap(respInsert).getData();
				log.info("Instancia de la ontologia insertada en BDTR:"+data);
				assertEquals(respInsert.getStatus(), 200);
				
			}catch(ResponseMapperException e){
				log.info(e.getMessage());
				assertEquals(false, true);
			}
			if(this.leave)	{	
				if(sessionkey!=null){
					//Genera un recurso SSAP con un LEAVE
					SSAPResource ssapLeave=new SSAPResource();
					ssapLeave.setLeave(true);
					ssapLeave.setSessionKey(sessionkey);
					Response respLeave=this.api.insert(ssapLeave);
					
					log.info("Codigo http retorno LEAVE: "+respLeave.getStatus()+" Para sessionKey: "+sessionkey);
					assertEquals(200, respLeave.getStatus());
					
				}
			}
			
		}
	

}
