package com.indra.sofia2.ssap.kp.implementations.rest.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * <p>
 * Java class for subscribe_response complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="subscribe_response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ok" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="subscriptionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "subscribe_response")
@XmlType(name = "subscribe_response")
public class SubscribeResponse extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String subscriptionId;
	
	
	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(this);
	}

	public static SubscribeResponse fromJsonToSubscribeResponse(String json) {
		return new JSONDeserializer<SubscribeResponse>().use(null,
				SubscribeResponse.class).deserialize(json);
	}

	public static String toJsonArray(Collection<SubscribeResponse> collection) {
		return new JSONSerializer().exclude("*.class").serialize(collection);
	}

	public static String toJsonArray(Collection<SubscribeResponse> collection,
			String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.serialize(collection);
	}

	public static Collection<SubscribeResponse> fromJsonArrayToSubscribeResponses(
			String json) {
		return new JSONDeserializer<List<SubscribeResponse>>()
				.use(null, ArrayList.class).use("values", SubscribeResponse.class)
				.deserialize(json);
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
