package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("PAOS", prefix = Namespaces.PAOS.PREFIX, namespace = Namespaces.PAOS.NS)
data class Paos(
	@XmlSerialName("mustUnderstand", namespace = Namespaces.SOAP.NS, prefix = Namespaces.SOAP.PREFIX)
	@XmlElement(false)
	val mustUnderstand: Boolean?,
	@XmlSerialName("actor", namespace = Namespaces.SOAP.NS, prefix = Namespaces.SOAP.PREFIX)
	@XmlElement(false)
	val actor: String,
	@SerialName("Version")
	@XmlElement
	val version: String?,
	@SerialName("EndpointReference")
	@XmlElement
	val endpointReference: EndpointReference?,
)
