package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticateResponse", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class DidAuthenticateResponse<T : AuthenticationProtocolData>(
	@SerialName("AuthenticationProtocolData")
	@XmlElement
	val data: T,
	override val result: Result,
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String,
) : ResponseType
