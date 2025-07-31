package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class ConnectionHandleType(
	override val ifdName: String? = null,
	override val slotIndex: UInt? = null,
	override val cardApplication: PrintableUByteArray? = null,
	@SerialName("ChannelHandle")
	@XmlElement
	override val channelHandle: ChannelHandleType? = null,
	@SerialName("ContextHandle")
	@XmlElement
	override val contextHandle: PrintableUByteArray? = null,
	@SerialName("SlotHandle")
	@XmlElement
	val slotHandle: PrintableUByteArray? = null,
	@SerialName("RecognitionInfo")
	@XmlElement
	val recognitionInfo: RecognitionInfoType? = null,
	val slotInfo: SlotInfoType? = null,
) : CardApplicationPathType
