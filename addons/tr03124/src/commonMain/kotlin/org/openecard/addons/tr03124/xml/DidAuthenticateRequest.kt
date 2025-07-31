package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticate", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class DidAuthenticateRequest<T : AuthenticationProtocolData>(
	override val requestId: String?,
	override val profile: String?,
	@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement
	val connectionHandle: ConnectionHandleType,
	@SerialName("DIDScope")
	@XmlElement
	val didScope: String?,
	@SerialName("DIDName")
	@XmlElement
	val didName: String,
	@SerialName("AuthenticationProtocolData")
	@XmlElement
	val data: T,
	@XmlSerialName("SAMConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement
	val samConnectionHandle: ConnectionHandleType?,
) : RequestType
