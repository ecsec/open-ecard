package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("Transmit", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class TransmitRequest(
	override val requestId: String?,
	override val profile: String?,
	@SerialName("SlotHandle")
	@XmlElement
	val slotHandle: PrintableUByteArray,
	@SerialName("InputAPDUInfo")
	@XmlElement
	val inputAPDUInfo: PrintableUByteArray,
) : RequestType
