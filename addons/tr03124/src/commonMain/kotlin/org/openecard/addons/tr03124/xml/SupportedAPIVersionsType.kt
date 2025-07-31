package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("SupportedAPIVersions", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class SupportedAPIVersionsType(
	@SerialName("Major")
	@XmlElement
	val major: Int,
	@SerialName("Minor")
	@XmlElement
	val minor: Int?,
	@SerialName("Subminor")
	@XmlElement
	val subminor: Int?,
)
