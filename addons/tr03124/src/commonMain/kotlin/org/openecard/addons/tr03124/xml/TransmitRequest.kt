package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("Transmit", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class TransmitRequest(
	@XmlSerialName("RequestID")
	@XmlElement(false)
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String? = ECardConstants.Profile.ECARD_1_1,
	@SerialName("SlotHandle")
	@XmlElement
	val slotHandle: PrintableUByteArray,
	@SerialName("InputAPDUInfo")
	@XmlElement
	val inputAPDUInfo: List<InputAPDUInfoType>,
) : RequestType
