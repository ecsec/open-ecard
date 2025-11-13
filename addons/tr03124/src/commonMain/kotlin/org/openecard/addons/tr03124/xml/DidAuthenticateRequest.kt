package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("DIDAuthenticate", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class DidAuthenticateRequest(
	@XmlSerialName("RequestID")
	@XmlElement(false)
	override val requestId: String?,
	@SerialName("Profile")
	@XmlElement(false)
	override val profile: String? = ECardConstants.Profile.ECARD_1_1,
	@XmlSerialName("ConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement
	val connectionHandle: ConnectionHandleType,
	@SerialName("DIDScope")
	@XmlElement
	val didScope: String?,
	@SerialName("DIDName")
	@XmlElement
	val didName: String,
	@Serializable(with = AuthenticationRequestProtocolDataSerializer::class)
	@SerialName("AuthenticationProtocolData")
	@XmlElement()
	val data: AuthenticationRequestProtocolData,
	@XmlSerialName("SAMConnectionHandle", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
	@XmlElement
	val samConnectionHandle: ConnectionHandleType?,
) : RequestType
