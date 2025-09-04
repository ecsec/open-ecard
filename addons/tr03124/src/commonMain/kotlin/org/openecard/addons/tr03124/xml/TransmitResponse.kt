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
	override val requestId: String? = null,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String? = ECardConstants.Profile.ECARD_1_1,
	@SerialName("OutputAPDU")
	@XmlElement
	val outputAPDU: List<PrintableUByteArray>,
) : ResponseType
