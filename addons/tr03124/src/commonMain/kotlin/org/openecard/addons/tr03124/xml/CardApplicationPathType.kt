package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.openecard.sc.iface.info.ApplicationIdentifier
import org.openecard.utils.serialization.PrintableUByteArray

sealed interface CardApplicationPathType {
	@SerialName("ChannelHandle")
	@XmlElement
	val channelHandle: ChannelHandleType?

	@SerialName("ContextHandle")
	@XmlElement
	val contextHandle: PrintableUByteArray?

	@SerialName("IFDName")
	@XmlElement
	val ifdName: String?

	@SerialName("SlotIndex")
	@XmlElement
	val slotIndex: UInt?

	@SerialName("CardApplication")
	@XmlElement
	val cardApplication: PrintableUByteArray?
}
