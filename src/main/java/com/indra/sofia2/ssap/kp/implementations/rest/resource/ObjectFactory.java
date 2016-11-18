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
package com.indra.sofia2.ssap.kp.implementations.rest.resource;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the v01.ssap.web.sofia2.indra.com package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {
	//Correc: Renombrada cosntante a mayuscula
    private final static QName _SSAP_QNAME = new QName("com.indra.sofia2.ssap.kp.implementations.rest.resource", "ssap");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: v01.ssap.web.sofia2.indra.com
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SSAPResource }
     * 
     */
    public SSAPResource createSsap() {
        return new SSAPResource();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SSAPResource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "com.indra.sofia2.ssap.kp.implementations.rest.resource", name = "ssap")
    public JAXBElement<SSAPResource> createSsap(SSAPResource value) {
        return new JAXBElement<SSAPResource>(_SSAP_QNAME, SSAPResource.class, null, value);
    }

}
