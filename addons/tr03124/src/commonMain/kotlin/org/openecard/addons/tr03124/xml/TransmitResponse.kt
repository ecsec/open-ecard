package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("TransmitResponse", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class TransmitResponse(
	override val result: Result,
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String,
	@SerialName("OutputAPDU")
	@XmlElement
	val outputAPDU: List<PrintableUByteArray>,
) : ResponseType
