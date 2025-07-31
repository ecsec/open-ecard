package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("StartPAOS", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class StartPaos(
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String?,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String,
	@SerialName("ConnectionHandle")
	@XmlElement
	val connectionHandle: ConnectionHandleType?,
	@SerialName("UserAgent")
	@XmlElement
	val userAgent: UserAgentType,
	@SerialName("SupportedAPIVersions")
	@XmlElement
	val supportedAPIVersions: List<SupportedAPIVersionsType>,
	@SerialName("SupportedDIDProtocols")
	@XmlElement
	val supportedDIDProtocols: String?,
) : RequestType
