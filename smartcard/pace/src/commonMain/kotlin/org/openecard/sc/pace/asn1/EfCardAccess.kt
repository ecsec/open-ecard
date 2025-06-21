package org.openecard.sc.pace.asn1

import org.openecard.sc.pace.asn1.SecurityInfo.Companion.toSecurityInfo
import org.openecard.sc.tlv.toTlvBer

class EfCardAccess
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val secInfos: Set<SecurityInfo>,
		val efCaData: UByteArray,
	) {
		val paceInfo: PaceInfo by lazy { secInfos.filterIsInstance<PaceInfo>().first() }

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun UByteArray.toEfCardAccess(): EfCardAccess {
				val tlv = this.toTlvBer().tlv
				val secInfos = tlv.toSecurityInfo()

				// TODO: add more plausability checks
				requireNotNull(secInfos.find { it is PaceInfo }) { "PaceInfo is missing in EF.CardAccess" }

				return EfCardAccess(secInfos, this)
			}
		}
	}
