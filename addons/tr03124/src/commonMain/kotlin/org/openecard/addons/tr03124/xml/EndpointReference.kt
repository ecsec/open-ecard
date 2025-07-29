package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EndpointReference", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
class EndpointReference(
	@SerialName("Address")
	@XmlElement
	val address: String?,
	@SerialName("MetaData")
	@XmlElement
	val metaData: String?,
)
