package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("EACAdditionalInputType", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class EacAdditionalInput(
	override val protocol: String,
	@SerialName("Signature")
	@XmlElement
	val signature: PrintableUByteArray,
) : AuthenticationRequestProtocolData
