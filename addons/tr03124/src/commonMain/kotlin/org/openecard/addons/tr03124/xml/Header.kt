package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable // (with = HeaderSerializer::class)
@XmlSerialName("Header", prefix = Namespaces.SOAP.PREFIX, namespace = Namespaces.SOAP.NS)
class Header(
	@SerialName("PAOS")
	@XmlElement
	val paos: Paos,
	@XmlSerialName("MessageID", namespace = "http://www.w3.org/2005/03/addressing")
	@XmlElement
	val messageID: String,
	@XmlSerialName("Action", namespace = "http://www.w3.org/2005/03/addressing")
	@XmlElement
	val action: String,
)
