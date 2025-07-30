package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class ConnectionHandleType(
	override val ifdName: String?,
	override val slotIndex: UInt?,
	override val cardApplication: PrintableUByteArray?,
	@SerialName("SlotHandle")
	@XmlElement
	val slotHandle: PrintableUByteArray?,
	@SerialName("ChannelHandle")
	@XmlElement
	override val channelHandle: ChannelHandleType?,
	@SerialName("ContextHandle")
	@XmlElement
	override val contextHandle: PrintableUByteArray?,
	@SerialName("RecognitionInfo")
	@XmlElement
	val recognitionInfo: RecognitionInfoType?,
	val slotInfo: SlotInfoType?,
) : CardApplicationPathType
