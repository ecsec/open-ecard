package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("EACAdditionalInputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class EacAdditionalInput(
	override val protocol: String,
) : AuthenticationProtocolData
