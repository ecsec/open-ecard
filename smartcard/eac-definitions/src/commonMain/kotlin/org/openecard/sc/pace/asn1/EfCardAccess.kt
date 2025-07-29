package org.openecard.sc.pace.asn1

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.InsufficientBuffer
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.InvalidParameter
import org.openecard.sc.iface.InvalidValue
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NotTransacted
import org.openecard.sc.iface.ProtoMismatch
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ResetCard
import org.openecard.sc.pace.asn1.SecurityInfo.Companion.toSecurityInfo
import org.openecard.sc.pace.oid.PaceObjectIdentifier
import org.openecard.sc.tlv.toTlvBer

class EfCardAccess
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val secInfos: Set<SecurityInfo>,
		val efCaData: UByteArray,
	) {
		val paceInfo by lazy {
			paceGroups.values.map { PaceInfos(it) }
		}

		private val paceGroups by lazy {
			secInfos.filterIsInstance<PaceParameterIdentifiable>().groupBy { it.parameterId }
		}

		class PaceInfos(
			paceGroup: List<PaceParameterIdentifiable>,
		) {
			val info = paceGroup.filterIsInstance<PaceInfo>().first()
			val params = paceGroup.filterIsInstance<PaceDomainParameterInfo>().firstOrNull()

			fun supports(
				protocols: Set<String>,
				domainParams: Set<UInt>,
			): Boolean = info.protocol.value in protocols && (params == null || params.parameterId in domainParams)
		}

		private val chipAuthGroups by lazy {
			secInfos.filterIsInstance<CaKeyIdentifiable>().groupBy { it.keyId }
		}

		val chipAuthenticationV1 by lazy {
			chipAuthGroups.values.filter { it.isCaV1() }.map { ChipAuthenticationV1(it) }
		}
		val chipAuthenticationV2 by lazy {
			chipAuthGroups.values.filter { it.isCaV2() }.map { ChipAuthenticationV2(it) }
		}
		val chipAuthenticationV3 by lazy {
			chipAuthGroups.values.filter { it.isCaV3() }.map { ChipAuthenticationV3(it) }
		}

		class ChipAuthenticationV1(
			caGroup: List<CaKeyIdentifiable>,
		) {
			val chipAuthenticationInfo = caGroup.filterIsInstance<ChipAuthenticationInfo>().first()
			// val chipAuthenticationPublicKeyInfo = caGroup.filterIsInstance<ChipAuthenticationPublicKeyInfo>().firstOrNull()
		}

		class ChipAuthenticationV2(
			caGroup: List<CaKeyIdentifiable>,
		) {
			val chipAuthenticationInfo = caGroup.filterIsInstance<ChipAuthenticationInfo>().first()

			// val chipAuthenticationPublicKeyInfo = caGroup.filterIsInstance<ChipAuthenticationPublicKeyInfo>().firstOrNull()
			val chipAuthenticationDomainParameterInfo = caGroup.filterIsInstance<ChipAuthenticationDomainParameterInfo>().first()
		}

		class ChipAuthenticationV3(
			caGroup: List<CaKeyIdentifiable>,
		) {
			val chipAuthenticationInfo = caGroup.filterIsInstance<ChipAuthenticationInfo>().first()
			val chipAuthenticationDomainParameterInfo = caGroup.filterIsInstance<ChipAuthenticationDomainParameterInfo>().first()
			val psaInfo = caGroup.filterIsInstance<PsaInfo>().first()
			// val psPublicKeyInfo = caGroup.filterIsInstance<PsPublicKeyInfo>().firstOrNull()
		}

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			val efCardAccessFileId: UShort = 0x011Cu
			val efCardAccessShortFileId: UByte = 0x1Cu

			@OptIn(ExperimentalUnsignedTypes::class)
			fun UByteArray.toEfCardAccess(): EfCardAccess {
				val tlv = this.toTlvBer().tlv
				val secInfos = tlv.toSecurityInfo()

				// TODO: add more plausibility checks
				requireNotNull(secInfos.find { it is PaceInfo }) { "PaceInfo is missing in EF.CardAccess" }

				return EfCardAccess(secInfos, this)
			}

			private fun List<CaKeyIdentifiable>.isCaV1(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 1u }

			private fun List<CaKeyIdentifiable>.isCaV2(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 2u }

			private fun List<CaKeyIdentifiable>.isCaV3(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 3u }

			@Throws(
				ApduProcessingError::class,
				InsufficientBuffer::class,
				InvalidHandle::class,
				InvalidParameter::class,
				InvalidValue::class,
				NoService::class,
				NotTransacted::class,
				ProtoMismatch::class,
				ReaderUnavailable::class,
				CommError::class,
				ResetCard::class,
				RemovedCard::class,
			)
			@OptIn(ExperimentalUnsignedTypes::class)
			fun readEfCardAccess(
				channel: CardChannel,
				forceShortEf: Boolean = false,
				efCaFileId: UShort = EfCardAccess.efCardAccessFileId,
				efCaShortFileId: UByte = EfCardAccess.efCardAccessShortFileId,
			): EfCardAccess {
				val extLen =
					channel.card.capabilities
						?.commandCoding
						?.supportsExtendedLength ?: false
				val useShortEf =
					forceShortEf ||
						channel.card.capabilities
							?.selectionMethods
							?.supportsShortEf ?: false
				val efCaData =
					if (useShortEf) {
						ReadBinary.readShortEf(efCaShortFileId, forceExtendedLength = extLen).transmit(channel)
					} else {
						Select.selectEfIdentifier(efCaFileId).transmit(channel)
						ReadBinary.readCurrentEf(forceExtendedLength = extLen).transmit(channel)
					}
				return efCaData.toEfCardAccess()
			}

			/**
			 * Pace Protocols supported by Open eCard
			 */
			val SUPPORTED_PACE_PROTOCOLS =
				setOf(
					PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
					PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
					PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
				)

			/**
			 * Pace Domain Parameters supported by Open eCard
			 */
			val SUPPORTED_PACE_DOMAIN_PARAMS =
				setOf(
					10u, // NIST P-224 (secp224r1)
					11u, // BrainpoolP224r1
					12u, // NIST P-256 (secp256r1)
					13u, // BrainpoolP256r1
					14u, // BrainpoolP320r1
					15u, // NIST P-384 (secp384r1)
					16u, // BrainpoolP384r1
					17u, // BrainpoolP512r1
					18u, // NIST P-521 (secp521r1)
				)
		}
	}
