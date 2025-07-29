package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
sealed interface AuthenticationProtocolData {
	@XmlSerialName("Protocol", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement(false)
	val protocol: String
}
