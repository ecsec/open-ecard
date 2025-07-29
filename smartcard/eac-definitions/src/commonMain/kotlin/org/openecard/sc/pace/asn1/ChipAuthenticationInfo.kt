package org.openecard.sc.pace.asn1

import dev.whyoleg.cryptography.bigint.decodeToBigInt
import dev.whyoleg.cryptography.bigint.toUInt
import dev.whyoleg.cryptography.serialization.asn1.Der
import dev.whyoleg.cryptography.serialization.asn1.modules.SubjectPublicKeyInfo
import kotlinx.serialization.decodeFromByteArray
import org.openecard.sc.pace.asn1.AlgorithmIdentifier.Companion.toAlgorithmIdentifier
import org.openecard.sc.pace.oid.CaObjectIdentifier
import org.openecard.sc.pace.oid.PkObjectIdentifier
import org.openecard.sc.pace.oid.PsaObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TagClass
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.toUInt

interface CaKeyIdentifiable {
	val keyId: UInt?
}

class ChipAuthenticationInfo(
	protocol: ObjectIdentifier,
	val version: UInt,
	override val keyId: UInt?,
) : SecurityInfo(protocol),
	CaKeyIdentifiable {
	companion object {
		val possibleProtocols =
			listOf(
				CaObjectIdentifier.id_CA_DH_3DES_CBC_CBC,
				CaObjectIdentifier.id_CA_DH_AES_CBC_CMAC_128,
				CaObjectIdentifier.id_CA_DH_AES_CBC_CMAC_192,
				CaObjectIdentifier.id_CA_DH_AES_CBC_CMAC_256,
				CaObjectIdentifier.id_CA_ECDH_3DES_CBC_CBC,
				CaObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_128,
				CaObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_192,
				CaObjectIdentifier.id_CA_ECDH_AES_CBC_CMAC_256,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toChipAuthenticationInfo(protocol: ObjectIdentifier): ChipAuthenticationInfo {
			val version = this[1].toUInt()
			val keyId = this.getOrNull(2)?.toUInt()
			return ChipAuthenticationInfo(protocol, version, keyId)
		}
	}
}

class ChipAuthenticationPublicKeyInfo(
	protocol: ObjectIdentifier,
	val chipAuthenticationPublicKey: SubjectPublicKeyInfo,
	override val keyId: UInt?,
) : SecurityInfo(protocol),
	CaKeyIdentifiable {
	companion object {
		val possibleProtocols =
			listOf(
				PkObjectIdentifier.id_PK_DH,
				PkObjectIdentifier.id_PK_ECDH,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toChipAuthenticationPublicKeyInfo(protocol: ObjectIdentifier): ChipAuthenticationPublicKeyInfo {
			val chipAuthenticationPublicKey: SubjectPublicKeyInfo = Der.decodeFromByteArray(this[1].toBer().toByteArray())
			val keyId = this.getOrNull(2)?.toUInt()
			return ChipAuthenticationPublicKeyInfo(protocol, chipAuthenticationPublicKey, keyId)
		}
	}
}

class ChipAuthenticationDomainParameterInfo(
	protocol: ObjectIdentifier,
	val domainParameter: AlgorithmIdentifier,
	override val keyId: UInt?,
) : SecurityInfo(protocol),
	CaKeyIdentifiable {
	companion object {
		val possibleProtocols =
			listOf(
				CaObjectIdentifier.id_CA_DH,
				CaObjectIdentifier.id_CA_ECDH,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toChipAuthenticationDomainParameterInfo(
			protocol: ObjectIdentifier,
		): ChipAuthenticationDomainParameterInfo {
			val domainParameter = this[1].toAlgorithmIdentifier()
			val keyId = this.getOrNull(2)?.toUInt()
			return ChipAuthenticationDomainParameterInfo(protocol, domainParameter, keyId)
		}
	}
}

class PsaInfo(
	protocol: ObjectIdentifier,
	val requiredData: RequiredData,
	override val keyId: UInt?,
) : SecurityInfo(protocol),
	CaKeyIdentifiable {
	class RequiredData(
		val version: Int,
		val ps1AuthInfo: AuthInfo,
		val ps2AuthInfo: AuthInfo,
	)

	enum class AuthInfo {
		NO_EXPLICIT_AUTH_REQUIRED,
		EXPLICIT_AUTH_REQUIRED,
		NOT_AUTHORIZED,
	}

	companion object {
		val possibleProtocols =
			listOf(
				PsaObjectIdentifier.ID_PSA_ECDH_ECSCHNORR_SHA256,
				PsaObjectIdentifier.ID_PSA_ECDH_ECSCHNORR_SHA384,
				PsaObjectIdentifier.ID_PSA_ECDH_ECSCHNORR_SHA512,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toPsaInfo(protocol: ObjectIdentifier): PsaInfo {
			// TODO: parse requiredData correctly

			val requiredData = this[1].toRequiredData()
			val keyId = this.getOrNull(2)?.toUInt()
			return PsaInfo(protocol, requiredData, keyId)
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		private fun Tlv.toRequiredData(): RequiredData {
			val children =
				this.asConstructed?.childList()
					?: throw IllegalArgumentException("PsaInfo.RequiredData is not a constructed tag")
			// PSARequiredData ::= SEQUENCE {
			//   version INTEGER, –- MUST be 1
			//   ps1-authInfo INTEGER (0 | 1 | 2),
			//   ps2-authInfo INTEGER (0 | 1 | 2)
			// }
			val version: Int = Der.decodeFromByteArray(children[0].toBer().toByteArray())
			val ps1 = Der.decodeFromByteArray<Int>(children[1].toBer().toByteArray()).toAuthInfo()
			val ps2 = Der.decodeFromByteArray<Int>(children[2].toBer().toByteArray()).toAuthInfo()
			return RequiredData(version, ps1, ps2)
		}

		private fun Int.toAuthInfo(): AuthInfo =
			when (this) {
				0 -> AuthInfo.NO_EXPLICIT_AUTH_REQUIRED
				1 -> AuthInfo.EXPLICIT_AUTH_REQUIRED
				2 -> AuthInfo.NOT_AUTHORIZED
				else -> throw IllegalArgumentException("Invalid value ($this) found for AuthInfo")
			}
	}
}

class PsPublicKeyInfo(
	protocol: ObjectIdentifier,
	val requiredData: SubjectPublicKeyInfo,
	val optionalData: PsPkOptionalData?,
) : SecurityInfo(protocol),
	CaKeyIdentifiable {
	override val keyId: UInt? by lazy {
		optionalData?.keyId
	}

	class PsPkOptionalData(
		val psParameterId: UInt?,
		val keyId: UInt?,
	)

	companion object {
		val possibleProtocols =
			listOf(
				PsaObjectIdentifier.ID_PS_PK_ECDH_ECSCHNORR,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toPsPublicKeyInfo(protocol: ObjectIdentifier): PsPublicKeyInfo {
			// PSPKRequiredData ::= SEQUENCE {
			//   pSPublicKey SubjectPublicKeyInfo,
			// }
			val requiredData: SubjectPublicKeyInfo = Der.decodeFromByteArray(this[1].contentAsBytesBer.toByteArray())

			// PSPKOptionalData ::= SEQUENCE {
			//   pSParameterID [1] IMPLICIT INTEGER OPTIONAL,
			//   keyId [2] IMPLICIT INTEGER OPTIONAL
			// }
			val optionalData =
				this.getOrNull(2)?.asConstructed?.let { data ->
					val paramId =
						data
							.findChildTags(Tag(TagClass.APPLICATION, true, 1u))
							.map {
								it.contentAsBytesBer
									.toByteArray()
									.decodeToBigInt()
									.toUInt()
							}.firstOrNull()
					val keyId =
						data
							.findChildTags(Tag(TagClass.APPLICATION, true, 2u))
							.map {
								it.contentAsBytesBer
									.toByteArray()
									.decodeToBigInt()
									.toUInt()
							}.firstOrNull()
					PsPkOptionalData(paramId, keyId)
				}

			return PsPublicKeyInfo(protocol, requiredData, optionalData)
		}
	}
}
