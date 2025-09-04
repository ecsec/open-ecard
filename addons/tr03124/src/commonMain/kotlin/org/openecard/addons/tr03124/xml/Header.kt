package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Header", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
data class Header(
	val paos: Paos,
	@XmlSerialName("MessageID", prefix = Namespaces.WSA.PREFIX, namespace = Namespaces.WSA.NS)
	@XmlElement
	val messageID: String? = null,
	@XmlSerialName("RelatesTo", prefix = Namespaces.WSA.PREFIX, namespace = Namespaces.WSA.NS)
	@XmlElement
	val relatesTo: String? = null,
	@XmlSerialName("Action", prefix = Namespaces.WSA.PREFIX, namespace = Namespaces.WSA.NS)
	@XmlElement
	val action: String? = null,
)
