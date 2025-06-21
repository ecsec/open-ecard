package org.openecard.sc.pace.asn1

import dev.whyoleg.cryptography.serialization.asn1.Der
import dev.whyoleg.cryptography.serialization.asn1.ObjectIdentifier
import kotlinx.serialization.decodeFromByteArray
import org.openecard.sc.pace.oid.PaceObjectIdentifier
import org.openecard.sc.tlv.Tlv

class PaceInfo(
	protocol: ObjectIdentifier,
	val version: Int,
	val parameterId: Int?,
) : SecurityInfo(protocol) {
	companion object {
		val possibleProtocols =
			listOf(
				PaceObjectIdentifier.id_PACE_DH_GM_3DES_CBC_CBC,
				PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_128,
				PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_192,
				PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_256,
				PaceObjectIdentifier.id_PACE_ECDH_GM_3DES_CBC_CBC,
				PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
				PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
				PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
				PaceObjectIdentifier.id_PACE_DH_IM_3DES_CBC_CBC,
				PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_128,
				PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_192,
				PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_256,
				PaceObjectIdentifier.id_PACE_ECDH_IM_3DES_CBC_CBC,
				PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_128,
				PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_192,
				PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_256,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toPaceInfo(protocol: ObjectIdentifier): PaceInfo {
			val version: Int = Der.decodeFromByteArray(this[1].toBer().toByteArray())
			val parameterId: Int? = this.getOrNull(2)?. let { Der.decodeFromByteArray(it.toBer().toByteArray()) }
			return PaceInfo(protocol, version, parameterId)
		}
	}
}
