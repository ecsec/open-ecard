package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("InitializeFrameworkResponse", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class InitializeFrameworkResponse(
	override val result: Result,
	override val requestId: String?,
	override val profile: String?,
) : ResponseType
