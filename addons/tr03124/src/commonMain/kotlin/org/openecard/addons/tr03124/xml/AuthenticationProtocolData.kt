package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
sealed interface AuthenticationProtocolData {
	@XmlSerialName("Protocol")
	@XmlElement(false)
	val protocol: String
}

@Serializable(AuthenticationRequestProtocolDataSerializer::class)
sealed interface AuthenticationRequestProtocolData : AuthenticationProtocolData

@Serializable
sealed interface AuthenticationResponseProtocolData : AuthenticationProtocolData
