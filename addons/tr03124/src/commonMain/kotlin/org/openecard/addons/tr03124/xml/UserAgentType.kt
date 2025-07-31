package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("UserAgent", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class UserAgentType(
	@SerialName("Name")
	@XmlElement
	val name: String,
	@SerialName("VersionMajor")
	@XmlElement
	val versionMajor: Int,
	@SerialName("VersionMinor")
	@XmlElement
	val versionMinor: Int,
	@SerialName("VersionSubminor")
	@XmlElement
	val versionSubminor: Int?,
)
