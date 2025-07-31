package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("EAC2OutputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class Eac2Output(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("EFCardSecurity")
	@XmlElement
	val eFCardSecurity: PrintableUByteArray,
	@SerialName("AuthenticationToken")
	@XmlElement
	val authenticationToken: PrintableUByteArray,
	@SerialName("Nonce")
	@XmlElement
	val nonce: PrintableUByteArray,
	@SerialName("Challenge")
	@XmlElement
	val challenge: PrintableUByteArray,
) : AuthenticationProtocolData
