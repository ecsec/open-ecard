package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticate", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class DidAuthenticateRequest<T : AuthenticationProtocolData>(
	val data: T,
	override val requestId: String?,
	override val profile: String?,
	@SerialName("AuthenticationProtocolData")
	@XmlElement
	val protocolData: AuthenticationProtocolData?,
	@SerialName("ConnectionHandle")
	@XmlElement
	val connectionHandle: ConnectionHandleType?,
	@SerialName("DIDName")
	@XmlElement
	val didName: String?,
) : RequestType
