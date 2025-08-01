package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("StartPAOS", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class StartPaos(
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String?,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String,
	@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement
	val connectionHandle: ConnectionHandleType?,
	@SerialName("UserAgent")
	@XmlSerialName("UserAgent", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	val userAgent: UserAgentType,
	@SerialName("SupportedAPIVersions")
	@XmlElement
	val supportedAPIVersions: List<SupportedAPIVersionsType>,
	@SerialName("SupportedDIDProtocols")
	@XmlElement
	val supportedDIDProtocols: List<String> = emptyList(),
) : RequestType
