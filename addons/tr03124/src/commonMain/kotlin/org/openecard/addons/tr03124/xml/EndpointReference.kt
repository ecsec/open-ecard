package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EndpointReference", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
class EndpointReference(
	@SerialName("Address")
	@XmlElement
	val address: String,
	@SerialName("PortType")
	@XmlElement
	@Contextual
	val portType: QName?,
	@SerialName("ServiceName ")
	@XmlElement
	@Contextual
	val serviceName: QName?,
	@SerialName("MetaData")
	@XmlElement
	@Contextual
	val metaData: Any?,
)
