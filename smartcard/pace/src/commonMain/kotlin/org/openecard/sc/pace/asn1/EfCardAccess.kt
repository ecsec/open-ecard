package org.openecard.sc.pace.asn1

import org.openecard.sc.pace.asn1.SecurityInfo.Companion.toSecurityInfo
import org.openecard.sc.tlv.toTlvBer

class EfCardAccess
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val secInfos: Set<SecurityInfo>,
		val efCaData: UByteArray,
	) {
		val paceInfo by lazy {
			secInfos.filterIsInstance<PaceInfo>().first()
		}
		val paceDomainParameterInfo by lazy {
			secInfos.filterIsInstance<PaceDomainParameterInfo>().firstOrNull()
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
			val chipAuthenticationPublicKeyInfo = caGroup.filterIsInstance<ChipAuthenticationPublicKeyInfo>().firstOrNull()
		}

		class ChipAuthenticationV2(
			caGroup: List<CaKeyIdentifiable>,
		) {
			val chipAuthenticationInfo = caGroup.filterIsInstance<ChipAuthenticationInfo>().first()
			val chipAuthenticationPublicKeyInfo = caGroup.filterIsInstance<ChipAuthenticationPublicKeyInfo>().firstOrNull()
			val chipAuthenticationDomainParameterInfo = caGroup.filterIsInstance<ChipAuthenticationDomainParameterInfo>().first()
		}

		class ChipAuthenticationV3(
			caGroup: List<CaKeyIdentifiable>,
		) {
			val chipAuthenticationInfo = caGroup.filterIsInstance<ChipAuthenticationInfo>().first()
			val chipAuthenticationDomainParameterInfo = caGroup.filterIsInstance<ChipAuthenticationDomainParameterInfo>().first()
			val psaInfo = caGroup.filterIsInstance<PsaInfo>().first()
			val psPublicKeyInfo = caGroup.filterIsInstance<PsPublicKeyInfo>().first()
		}

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun UByteArray.toEfCardAccess(): EfCardAccess {
				val tlv = this.toTlvBer().tlv
				val secInfos = tlv.toSecurityInfo()

				// TODO: add more plausibility checks
				requireNotNull(secInfos.find { it is PaceInfo }) { "PaceInfo is missing in EF.CardAccess" }

				return EfCardAccess(secInfos, this)
			}

			private fun List<CaKeyIdentifiable>.isCaV1(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 1 }

			private fun List<CaKeyIdentifiable>.isCaV2(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 2 }

			private fun List<CaKeyIdentifiable>.isCaV3(): Boolean =
				this.filterIsInstance<ChipAuthenticationInfo>().any { it.version == 3 }
		}
	}
