package org.openecard.addons.tr03124

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("TCTokenType")
class TcToken(
	@SerialName("ServerAddress")
	@XmlElement
	val serverAddress: String,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String,
	@SerialName("RefreshAddress")
	@XmlElement
	val refreshAddress: String,
	@SerialName("CommunicationErrorAddress")
	@XmlElement
	val communicationErrorAddress: String?,
	val binding: BindingType,
	val securityProtocol: SecurityProtocolType?,
	val securityParameters: PskParams?,
) {
	@SerialName("Binding")
	enum class BindingType {
		@SerialName("urn:liberty:paos:2006-08")
		PAOS,
	}

	@SerialName("PathSecurity-Protocol")
	enum class SecurityProtocolType {
		@SerialName("urn:ietf:rfc:4279")
		TLS_PSK,
	}

	// Polymorphic serializer is very tricky as discriminator (PathSecurity-Protocol) is in the parent element
	// When only doing nPA, we don't need this, so skip it for now
	@Serializable
	sealed interface SecurityParameters

	@Serializable
	@SerialName("PathSecurity-Parameters")
	class PskParams
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			@SerialName("PSK")
			@XmlElement
			val psk: HexString,
		) : SecurityParameters

	companion object {
		val Xml =
			XML {
				xmlDeclMode = XmlDeclMode.None
				defaultPolicy {
					this.verifyElementOrder = false
				}
			}

		fun String.toTcToken(): TcToken = Xml.decodeFromString(this)
	}
}
