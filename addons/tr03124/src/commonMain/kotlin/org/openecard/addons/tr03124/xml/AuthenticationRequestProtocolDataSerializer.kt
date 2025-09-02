package org.openecard.addons.tr03124.xml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.QNameSerializer
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

private inline fun <reified T> T?.require(name: String): T {
	if (this == null) {
		throw SerializationException("Missing field ($name) in AuthenticationProtocolData")
	} else {
		return this
	}
}

@Serializable
@XmlSerialName("AuthenticationProtocolData", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
private data class AuthenticationRequestProtocolDataInternal(
	//
	// discriminator
	//
	@XmlSerialName("type", namespace = Namespaces.XSI.NS, prefix = Namespaces.XSI.PREFIX)
	@XmlElement(false)
	@Serializable(with = QNameSerializer::class)
	val type: QName,
	//
	// base type values
	//
	@SerialName("Protocol")
	@XmlElement(false)
	val protocol: String,
	//
	// EAC1Input
	//
	@SerialName("Certificate")
	@XmlElement
	val certificates: List<PrintableUByteArray> = listOf(),
	@SerialName("CertificateDescription")
	@XmlElement
	val certificateDescription: PrintableUByteArray? = null,
	@SerialName("RequiredCHAT")
	@XmlElement
	val requiredChat: PrintableUByteArray? = null,
	@SerialName("OptionalCHAT")
	@XmlElement
	val optionalChat: PrintableUByteArray? = null,
	@SerialName("AuthenticatedAuxiliaryData")
	@XmlElement
	val authenticatedAuxiliaryData: PrintableUByteArray? = null,
	@SerialName("TransactionInfo")
	@XmlElement
	val transactionInfo: String? = null,
	@SerialName("AcceptedEIDType")
	@XmlElement
	val acceptedEidType: List<String> = emptyList(),
	//
	// EAC2Input
	//
	// 	@SerialName("Certificate")
	// 	@XmlElement
	// 	val certificate: List<PrintableUByteArray>?,
	@SerialName("EphemeralPublicKey")
	@XmlElement
	val ephemeralPublicKey: PrintableUByteArray? = null,
	@SerialName("Signature")
	@XmlElement
	val signature: PrintableUByteArray? = null,
	//
	// AdditionalInput
	//
	// 	@SerialName("Signature")
	// 	@XmlElement
	// 	val signature: PrintableUByteArray,
) {
	fun toPublicType(): AuthenticationRequestProtocolData {
		if (type.namespaceURI != Namespaces.ISO.NS) {
			throw SerializationException("Invalid namespace (${type.namespaceURI}) in type attribute")
		}
		return when (type.localPart) {
			"EAC1InputType" -> {
				Eac1Input(
					protocol = protocol,
					certificates = certificates,
					certificateDescription = certificateDescription.require("CertificateDescription"),
					requiredChat = requiredChat,
					optionalChat = optionalChat,
					authenticatedAuxiliaryData = authenticatedAuxiliaryData,
					transactionInfo = transactionInfo,
					acceptedEidType = acceptedEidType,
				)
			}
			"EAC2InputType" -> {
				Eac2Input(
					protocol = protocol,
					certificates = certificates,
					ephemeralPublicKey = ephemeralPublicKey.require("EphemeralPublicKey"),
					signature = signature,
				)
			}
			"EACAdditionalInputType" -> {
				EacAdditionalInput(
					protocol = protocol,
					signature = signature.require("Signature"),
				)
			}
			else -> throw SerializationException("Unknown protocol data type ${type.localPart}")
		}
	}

	companion object {
		fun fromPublicType(public: AuthenticationRequestProtocolData): AuthenticationRequestProtocolDataInternal =
			when (public) {
				is Eac1Input ->
					AuthenticationRequestProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EAC1InputType"),
						protocol = public.protocol,
						certificates = public.certificates,
						certificateDescription = public.certificateDescription,
						requiredChat = public.requiredChat,
						optionalChat = public.optionalChat,
						authenticatedAuxiliaryData = public.authenticatedAuxiliaryData,
						transactionInfo = public.transactionInfo,
						acceptedEidType = public.acceptedEidType,
					)
				is Eac2Input ->
					AuthenticationRequestProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EAC2InputType"),
						protocol = public.protocol,
						certificates = public.certificates,
						ephemeralPublicKey = public.ephemeralPublicKey,
						signature = public.signature,
					)
				is EacAdditionalInput ->
					AuthenticationRequestProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EACAdditionalInputType"),
						protocol = public.protocol,
						signature = public.signature,
					)
			}
	}
}

object AuthenticationRequestProtocolDataSerializer : KSerializer<AuthenticationRequestProtocolData> {
	override val descriptor = AuthenticationRequestProtocolDataInternal.serializer().descriptor

	override fun deserialize(decoder: Decoder): AuthenticationRequestProtocolData =
		AuthenticationRequestProtocolDataInternal.serializer().deserialize(decoder).toPublicType()

	override fun serialize(
		encoder: Encoder,
		value: AuthenticationRequestProtocolData,
	): Unit =
		AuthenticationRequestProtocolDataInternal.serializer().serialize(
			encoder,
			AuthenticationRequestProtocolDataInternal.fromPublicType(value),
		)
}
