package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC2OutputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac2Output(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("EFCardAccess")
	@XmlElement
	val efCardAccess: String?,
	@SerialName("EFCardSecurity")
	@XmlElement
	val eFCardSecurity: String?,
	@SerialName("AuthenticationToken")
	@XmlElement
	val authenticationToken: String?,
	@SerialName("Nonce")
	@XmlElement
	val nonce: String?,
) : AuthenticationProtocolData
