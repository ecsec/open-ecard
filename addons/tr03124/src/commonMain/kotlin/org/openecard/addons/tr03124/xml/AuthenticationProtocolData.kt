package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
sealed interface AuthenticationProtocolData {
	@XmlElement(false)
	@XmlSerialName("Protocol", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	val protocol: String
}
