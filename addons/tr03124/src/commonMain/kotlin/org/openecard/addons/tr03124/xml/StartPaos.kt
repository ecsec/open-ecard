package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("StartPAOS", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class StartPaos(
	override val requestId: String?,
	override val profile: String?,
	@XmlSerialName("SessionIdentifier")
	val sessionId: String,
) : RequestType
