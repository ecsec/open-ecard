package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("PAOS", prefix = Namespaces.SOAP.PREFIX, namespace = "urn:liberty:paos:2006-08")
class Paos(
	@XmlSerialName("mustUnderstand", namespace = Namespaces.SOAP.NS)
	@XmlElement(false)
	val mustUnderstand: Int,
	@XmlSerialName("actor", namespace = Namespaces.SOAP.NS)
	@XmlElement(false)
	val actor: String,
	@XmlSerialName("Version", namespace = "urn:liberty:paos:2006-08")
	@XmlElement
	val version: String,
	@XmlSerialName("EndpointReference", namespace = "urn:liberty:paos:2006-08")
	@XmlElement
	val endpointReference: EndpointReference,
)
