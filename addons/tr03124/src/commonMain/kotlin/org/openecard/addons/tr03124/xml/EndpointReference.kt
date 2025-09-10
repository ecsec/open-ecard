package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.QNameSerializer
import nl.adaptivity.xmlutil.serialization.SerializableElement
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("EndpointReference", prefix = Namespaces.WSA.PREFIX, namespace = Namespaces.WSA.NS)
data class EndpointReference(
	@SerialName("Address")
	@XmlElement
	val address: String,
	@SerialName("PortType")
	@XmlElement
	@Serializable(with = QNameSerializer::class)
	val portType: QName? = null,
	@SerialName("ServiceName")
	val serviceName: ServiceName? = null,
)

@Serializable
data class ServiceName(
	@XmlElement(false)
	val portName: String? = null,
	@XmlValue
	@Serializable(with = QNameSerializer::class)
	val name: QName,
)
