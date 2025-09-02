package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("EAC2InputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class Eac2Input(
	@SerialName("Protocol")
	@XmlElement(false)
	override val protocol: String,
	@SerialName("Certificate")
	@XmlElement
	val certificates: List<PrintableUByteArray>,
	@SerialName("EphemeralPublicKey")
	@XmlElement
	val ephemeralPublicKey: PrintableUByteArray,
	@SerialName("Signature")
	@XmlElement
	val signature: PrintableUByteArray?,
) : AuthenticationRequestProtocolData
