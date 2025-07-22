package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("TransmitResponse", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class TransmitResponse(
	override val result: Result,
	override val requestId: String?,
	override val profile: String,
) : ResponseType
