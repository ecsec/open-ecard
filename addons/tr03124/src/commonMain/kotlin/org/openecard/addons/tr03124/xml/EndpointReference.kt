package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.QNameSerializer
import nl.adaptivity.xmlutil.serialization.SerializableElement
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EndpointReference", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
data class EndpointReference(
	@SerialName("Address")
	@XmlElement
	val address: String,
	@SerialName("PortType")
	@XmlElement
	@Serializable(with = QNameSerializer::class)
	val portType: QName?,
	@SerialName("ServiceName")
	@XmlElement
	@Serializable(with = QNameSerializer::class)
	val serviceName: QName?,
	@SerialName("MetaData")
	@XmlElement
	val metaData: SerializableElement?,
)
