package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("SlotInfo", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class SlotInfoType(
	@SerialName("ProtectedAuthPath")
	@XmlElement
	val protectedAuthPath: Boolean?,
)
