package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticate", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class DidAuthenticateRequest<T : AuthenticationProtocolData>(
	@SerialName("AuthenticationProtocolData")
	@XmlElement
	val data: T,
	override val requestId: String?,
	override val profile: String?,
	@SerialName("ConnectionHandle")
	@XmlElement
	val connectionHandle: ConnectionHandleType,
	@SerialName("DIDName")
	@XmlElement
	val didName: String,
	@SerialName("DIDScope")
	@XmlElement
	val didScope: String?,
	@SerialName("SAMConnectionHandle")
	@XmlElement
	val samConnectionHandle: ConnectionHandleType?,
) : RequestType
