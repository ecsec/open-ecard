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
private data class AuthenticationResponseProtocolDataInternal(
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
	// EAC1Output
	//
	@SerialName("CertificateHolderAuthorizationTemplate")
	@XmlElement
	val certificateHolderAuthorizationTemplate: PrintableUByteArray? = null,
	@SerialName("CertificationAuthorityReference")
	@XmlElement
	val certificationAuthorityReference: List<String> = emptyList(),
	@SerialName("EFCardAccess")
	@XmlElement
	val efCardAccess: PrintableUByteArray? = null,
	@SerialName("IDPICC")
	@XmlElement
	val idPICC: PrintableUByteArray? = null,
	@SerialName("Challenge")
	@XmlElement
	val challenge: PrintableUByteArray? = null,
	//
	// EAC2Output
	//
	@SerialName("EFCardSecurity")
	@XmlElement
	val efCardSecurity: PrintableUByteArray? = null,
	@SerialName("AuthenticationToken")
	@XmlElement
	val authenticationToken: PrintableUByteArray? = null,
	@SerialName("Nonce")
	@XmlElement
	val nonce: PrintableUByteArray? = null,
	// 	@SerialName("Challenge")
	// 	@XmlElement
	// 	val challenge: PrintableUByteArray?,
	//
	// EmptyResponseDataType (has no data)
	//
) {
	fun toPublicType(): AuthenticationResponseProtocolData {
		if (type.namespaceURI != Namespaces.ISO.NS) {
			throw SerializationException("Invalid namespace (${type.namespaceURI}) in type attribute")
		}
		return when (type.localPart) {
			"EAC1OutputType" -> {
				Eac1Output(
					protocol = protocol,
					certificateHolderAuthorizationTemplate = certificateHolderAuthorizationTemplate,
					certificationAuthorityReference = certificationAuthorityReference,
					efCardAccess = efCardAccess.require("EFCardAccess"),
					idPICC = idPICC.require("IDPICC"),
					challenge = challenge.require("Challenge"),
				)
			}
			"EAC2OutputType" -> {
				Eac2Output(
					protocol = protocol,
					efCardSecurity = efCardSecurity.require("EFCardSecurity"),
					authenticationToken = authenticationToken.require("AuthenticationToken"),
					nonce = nonce.require("Nonce"),
					challenge = challenge,
				)
			}
			"EmptyResponseDataType" -> {
				EmptyResponseDataType(
					protocol = protocol,
				)
			}
			else -> throw SerializationException("Unknown protocol data type ${type.localPart}")
		}
	}

	companion object {
		fun fromPublicType(public: AuthenticationResponseProtocolData): AuthenticationResponseProtocolDataInternal =
			when (public) {
				is Eac1Output ->
					AuthenticationResponseProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EAC1OutputType"),
						protocol = public.protocol,
						certificateHolderAuthorizationTemplate = public.certificateHolderAuthorizationTemplate,
						certificationAuthorityReference = public.certificationAuthorityReference,
						efCardAccess = public.efCardAccess,
						idPICC = public.idPICC,
						challenge = public.challenge,
					)
				is Eac2Output ->
					AuthenticationResponseProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EAC2OutputType"),
						protocol = public.protocol,
						efCardSecurity = public.efCardSecurity,
						authenticationToken = public.authenticationToken,
						nonce = public.nonce,
						challenge = public.challenge,
					)
				is EmptyResponseDataType ->
					AuthenticationResponseProtocolDataInternal(
						type = QName(Namespaces.ISO.NS, "EmptyResponseDataType"),
						protocol = public.protocol,
					)
			}
	}
}

object AuthenticationResponseProtocolDataSerializer : KSerializer<AuthenticationResponseProtocolData> {
	override val descriptor = AuthenticationResponseProtocolDataInternal.serializer().descriptor

	override fun deserialize(decoder: Decoder): AuthenticationResponseProtocolData =
		AuthenticationResponseProtocolDataInternal.serializer().deserialize(decoder).toPublicType()

	override fun serialize(
		encoder: Encoder,
		value: AuthenticationResponseProtocolData,
	): Unit =
		AuthenticationResponseProtocolDataInternal.serializer().serialize(
			encoder,
			AuthenticationResponseProtocolDataInternal.fromPublicType(value),
		)
}
