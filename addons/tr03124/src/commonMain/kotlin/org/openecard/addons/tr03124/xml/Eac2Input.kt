package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EAC2InputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac2Input(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("Certificate")
	@XmlElement
	val certificate: String?,
	@SerialName("EphemeralPublicKey")
	@XmlElement
	val ephemeralPublicKey: String?,
	@SerialName("Signature")
	@XmlElement
	val signature: String?,
) : AuthenticationProtocolData
