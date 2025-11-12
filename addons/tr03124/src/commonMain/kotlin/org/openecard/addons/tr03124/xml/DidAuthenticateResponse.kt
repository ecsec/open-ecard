package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticateResponse", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class DidAuthenticateResponse(
	override val result: Result,
	@SerialName("AuthenticationProtocolData")
	@XmlElement
	@Serializable(with = AuthenticationResponseProtocolDataSerializer::class)
	val data: AuthenticationResponseProtocolData,
	override val requestId: String? = null,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String? = ECardConstants.Profile.ECARD_1_1,
) : ResponseType
