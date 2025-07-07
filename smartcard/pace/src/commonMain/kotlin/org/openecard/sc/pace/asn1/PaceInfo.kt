package org.openecard.sc.pace.asn1

import dev.whyoleg.cryptography.serialization.asn1.Der
import kotlinx.serialization.decodeFromByteArray
import org.openecard.sc.pace.crypto.StandardizedDomainParameters
import org.openecard.sc.pace.oid.PaceObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tlv

interface PaceParameterIdentifiable {
	val parameterId: UInt?
}

class PaceInfo(
	protocol: ObjectIdentifier,
	val version: Int,
	override val parameterId: UInt?,
) : SecurityInfo(protocol),
	PaceParameterIdentifiable {
	val isStandardizedParameter: Boolean
		get() {
			val id = parameterId ?: UInt.MAX_VALUE
			return id >= 0u && id <= 31u
		}

	val standardizedDomainParameters: StandardizedDomainParameters by lazy {
		check(isStandardizedParameter)
		StandardizedDomainParameters.forParameterId(parameterId!!)
	}

	val kdfLength: Int by lazy {
		when (protocol.value) {
			PaceObjectIdentifier.id_PACE_DH_GM_3DES_CBC_CBC,
			PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_128,
			PaceObjectIdentifier.id_PACE_DH_IM_3DES_CBC_CBC,
			PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_128,
			PaceObjectIdentifier.id_PACE_ECDH_GM_3DES_CBC_CBC,
			PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_128,
			PaceObjectIdentifier.id_PACE_ECDH_IM_3DES_CBC_CBC,
			PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_128,
			-> 16

			PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_192,
			PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_192,
			PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_192,
			PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_192,
			-> 24

			PaceObjectIdentifier.id_PACE_DH_GM_AES_CBC_CMAC_256,
			PaceObjectIdentifier.id_PACE_DH_IM_AES_CBC_CMAC_256,
			PaceObjectIdentifier.id_PACE_ECDH_GM_AES_CBC_CMAC_256,
			PaceObjectIdentifier.id_PACE_ECDH_IM_AES_CBC_CMAC_256,
			-> 32

			else -> throw IllegalArgumentException("Unknown PACE protocol: $protocol")
		}
	}

	/**
	 * Checks if the protocol identifier indicates generic mapping.
	 *
	 * @return True if generic mapping is used, otherwise false
	 */
	val isGm: Boolean by lazy {

		protocol.value.startsWith(PaceObjectIdentifier.id_PACE_DH_GM) ||
			protocol.value.startsWith(PaceObjectIdentifier.id_PACE_ECDH_GM)
	}

	/**
	 * Checks if the protocol identifier indicates integrated mapping.
	 *
	 * @return True if integrated mapping is used, otherwise false
	 */
	val isIm: Boolean by lazy {
		protocol.value.startsWith(PaceObjectIdentifier.id_PACE_DH_IM) ||
			protocol.value.startsWith(PaceObjectIdentifier.id_PACE_ECDH_IM)
	}

	/**
	 * Checks if the protocol identifier indicates Diffie-Hellman.
	 *
	 * @return True if Diffie-Hellman is used, otherwise false
	 */
	val isDh: Boolean by lazy {
		protocol.value.startsWith(PaceObjectIdentifier.id_PACE_DH_GM) ||
			protocol.value.startsWith(PaceObjectIdentifier.id_PACE_DH_IM)
	}

	/**
	 * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
	 *
	 * @return True if elliptic curve Diffie-Hellman is used, otherwise false
	 */
	val isEcdh: Boolean by lazy {
		protocol.value.startsWith(PaceObjectIdentifier.id_PACE_ECDH_GM) ||
			protocol.value.startsWith(PaceObjectIdentifier.id_PACE_ECDH_IM)
	}

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
			val parameterId: UInt? = this.getOrNull(2)?. let { Der.decodeFromByteArray(it.toBer().toByteArray()) }
			return PaceInfo(protocol, version, parameterId)
		}
	}
}

class PaceDomainParameterInfo(
	protocol: ObjectIdentifier,
	val domainParameter: AlgorithmIdentifier,
	override val parameterId: UInt?,
) : SecurityInfo(protocol),
	PaceParameterIdentifiable {
	companion object {
		val possibleProtocols =
			listOf(
				PaceObjectIdentifier.id_PACE_DH_GM,
				PaceObjectIdentifier.id_PACE_ECDH_GM,
				PaceObjectIdentifier.id_PACE_DH_IM,
				PaceObjectIdentifier.id_PACE_ECDH_IM,
			)

		fun isResponsible(protocol: ObjectIdentifier): Boolean = protocol.value in possibleProtocols

		@OptIn(ExperimentalUnsignedTypes::class)
		fun List<Tlv>.toPaceDomainParameterInfo(protocol: ObjectIdentifier): PaceDomainParameterInfo {
			val domainParameter: AlgorithmIdentifier = Der.decodeFromByteArray(this[1].toBer().toByteArray())
			val parameterId: UInt? = this.getOrNull(2)?. let { Der.decodeFromByteArray(it.toBer().toByteArray()) }
			return PaceDomainParameterInfo(protocol, domainParameter, parameterId)
		}
	}
}
