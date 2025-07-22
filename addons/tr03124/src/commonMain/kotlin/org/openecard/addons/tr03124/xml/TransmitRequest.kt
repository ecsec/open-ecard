package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Transmit", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class TransmitRequest(
	override val requestId: String?,
	override val profile: String?,
) : RequestType
