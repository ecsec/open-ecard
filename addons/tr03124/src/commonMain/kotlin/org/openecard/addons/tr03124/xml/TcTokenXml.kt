package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray
import java.lang.IllegalArgumentException

@Serializable
@SerialName("TCTokenType")
open class TcTokenXml(
	@SerialName("ServerAddress")
	@XmlElement
	val serverAddress: String?,
	@SerialName("SessionIdentifier")
	@XmlElement
	val sessionIdentifier: String?,
	@SerialName("RefreshAddress")
	@XmlElement
	val refreshAddress: String?,
	@SerialName("CommunicationErrorAddress")
	@XmlElement
	val communicationErrorAddress: String?,
	@XmlSerialName("Binding")
	@XmlElement
	val binding: String?,
	@XmlSerialName("PathSecurity-Protocol")
	@XmlElement
	val securityProtocol: String?,
	@XmlSerialName("PathSecurity-Parameters")
	val securityParameters: PskParams?,
) {
	enum class BindingType(
		val constant: String,
	) {
		@SerialName("urn:liberty:paos:2006-08")
		PAOS("urn:liberty:paos:2006-08"),
	}

	enum class SecurityProtocolType(
		val constant: String,
	) {
		@SerialName("urn:ietf:rfc:4279")
		TLS_PSK("urn:ietf:rfc:4279"),
	}

	// Polymorphic serializer is very tricky as discriminator (PathSecurity-Protocol) is in the parent element
	// When only doing nPA, we don't need this, so skip it for now
	@Serializable
	sealed interface SecurityParameters

	@Serializable
	class PskParams
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			@SerialName("PSK")
			@XmlElement
			val psk: PrintableUByteArray,
		) : SecurityParameters

	companion object {
		fun String.parseTcToken(): TcTokenXml = tcTokenXml.decodeFromString(this)

		fun String.toBindingType(): BindingType =
			BindingType.entries.find { it.constant == this }
				?: throw IllegalArgumentException("Invalid Binding value specified: $this")

		fun String.toSecurityProtocolType(): SecurityProtocolType =
			SecurityProtocolType.entries.find { it.constant == this }
				?: throw IllegalArgumentException("Invalid SecurityProtocol value specified: $this")
	}
}
