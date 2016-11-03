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
package com.indra.sofia2.ssap.kp.implementations.rest.response;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * <p>
 * Java class for update_response complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="update_response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="affectedRecords" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ok" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "update_response")
@XmlType(name = "update_response")
public class UpdateResponse extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String ontologiesInstancesIds;
	protected int affectedRecords;

	/**
	 * Gets the value of the affectedRecords property.
	 * 
	 */
	public int getAffectedRecords() {
		return affectedRecords;
	}

	/**
	 * Sets the value of the affectedRecords property.
	 * 
	 */
	public void setAffectedRecords(int value) {
		this.affectedRecords = value;
	}

	public String getOntologiesInstancesIds() {
		return ontologiesInstancesIds;
	}

	public void setOntologiesInstancesIds(String ontologiesInstancesIds) {
		this.ontologiesInstancesIds = ontologiesInstancesIds;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
