package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class ConnectionHandleType(
	@SerialName("SlotHandle")
	@XmlElement
	val slotHandle: String?,
	@SerialName("ChannelHandle")
	@XmlElement
	val channelHandle: String?,
	@SerialName("ContextHandle")
	@XmlElement
	val contextHandle: String?,
)
