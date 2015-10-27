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
package com.indra.sofia2.ssap.kp.implementations.rest.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ssap complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ssap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="data" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instanceKP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="join" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="leave" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ontology" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sessionKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ssap", propOrder = {
    "data",
    "instanceKP",
    "join",
    "leave",
    "ontology",
    "sessionKey",
    "token"
})
public class SSAPResource {

    protected String data;
    protected String instanceKP;
    protected boolean join;
    protected boolean leave;
    protected String ontology;
    protected String sessionKey;
    protected String token;

    /**
     * Obtiene el valor de la propiedad data.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getData() {
        return data;
    }

    /**
     * Define el valor de la propiedad data.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setData(String value) {
        this.data = value;
    }

    /**
     * Obtiene el valor de la propiedad instanceKP.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceKP() {
        return instanceKP;
    }

    /**
     * Define el valor de la propiedad instanceKP.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceKP(String value) {
        this.instanceKP = value;
    }

    /**
     * Obtiene el valor de la propiedad join.
     * 
     */
    public boolean isJoin() {
        return join;
    }

    /**
     * Define el valor de la propiedad join.
     * 
     */
    public void setJoin(boolean value) {
        this.join = value;
    }

    /**
     * Obtiene el valor de la propiedad leave.
     * 
     */
    public boolean isLeave() {
        return leave;
    }

    /**
     * Define el valor de la propiedad leave.
     * 
     */
    public void setLeave(boolean value) {
        this.leave = value;
    }

    /**
     * Obtiene el valor de la propiedad ontology.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOntology() {
        return ontology;
    }

    /**
     * Define el valor de la propiedad ontology.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOntology(String value) {
        this.ontology = value;
    }

    /**
     * Obtiene el valor de la propiedad sessionKey.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Define el valor de la propiedad sessionKey.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionKey(String value) {
        this.sessionKey = value;
    }

    /**
     * Obtiene el valor de la propiedad token.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Define el valor de la propiedad token.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

	@Override
	public String toString() {
		return "SSAPResource [data=" + data + ", instanceKP=" + instanceKP
				+ ", join=" + join + ", leave=" + leave + ", ontology="
				+ ontology + ", sessionKey=" + sessionKey + ", token=" + token
				+ "]";
	}
    
    

}
