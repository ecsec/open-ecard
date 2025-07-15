package org.openecard.sc.pace.asn1

import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.toTlvBer

object GeneralAuthenticateResponse {
	class EncryptedNonce
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val encryptedNonce: UByteArray,
		)

	class MapNonce
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val mappingData: UByteArray,
		)

	class KeyAgreement
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val ephemeralPubKey: UByteArray,
		)

	class AuthenticationToken
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val at: UByteArray,
			val curCar: UByteArray?,
			val prevCar: UByteArray?,
		)

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun ResponseApdu.toDynamicAuthenticationData(): List<Tlv> {
		val tlv = this.data.toTlvBer().tlv
		if (tlv.tag != GeneralAuthenticateResponseTags.dynamicAuthenticationData) {
			throw PaceError(PaceResultCode.UNEXPECTED_TLV_RESPONSE_OBJECT, null)
		}
		return checkNotNull(tlv.asConstructed).childList()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun ResponseApdu.toEncryptedNonce(): EncryptedNonce =
		this.toDynamicAuthenticationData().let { dos ->
			val encryptedNonce =
				dos.findTlv(GeneralAuthenticateResponseTags.encryptedNonce)?.contentAsBytesBer
					?: throw PaceError(PaceResultCode.UNEXPECTED_TLV_RESPONSE_OBJECT, null)
			EncryptedNonce(encryptedNonce)
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun ResponseApdu.toMapNonce(): MapNonce =
		this.toDynamicAuthenticationData().let { dos ->
			val mappingData =
				dos.findTlv(GeneralAuthenticateResponseTags.mappingData)?.contentAsBytesBer
					?: throw PaceError(PaceResultCode.UNEXPECTED_TLV_RESPONSE_OBJECT, null)
			MapNonce(mappingData)
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun ResponseApdu.toKeyAgreement(): KeyAgreement =
		this.toDynamicAuthenticationData().let { dos ->
			val ephemeralPubKey =
				dos.findTlv(GeneralAuthenticateResponseTags.ephemeralPublicKey)?.contentAsBytesBer
					?: throw PaceError(PaceResultCode.UNEXPECTED_TLV_RESPONSE_OBJECT, null)
			KeyAgreement(ephemeralPubKey)
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun ResponseApdu.toAuthenticationToken(): AuthenticationToken =
		this.toDynamicAuthenticationData().let { dos ->
			val at =
				dos.findTlv(GeneralAuthenticateResponseTags.authenticationToken)?.contentAsBytesBer
					?: throw PaceError(PaceResultCode.UNEXPECTED_TLV_RESPONSE_OBJECT, null)
			val curCar =
				dos.findTlv(GeneralAuthenticateResponseTags.curCar)?.contentAsBytesBer
			val prevCar =
				dos.findTlv(GeneralAuthenticateResponseTags.prevCar)?.contentAsBytesBer
			AuthenticationToken(at, curCar, prevCar)
		}
}
