package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse

interface PaceCryptoSuite {
	val paceInfos: EfCardAccess.PaceInfos

	fun start(): Step1

	interface Step1 {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun decryptNonce(encryptedNonce: GeneralAuthenticateResponse.EncryptedNonce): Step2
	}

	interface Step2 {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun mapPublicKeyPcd(): UByteArray

		@OptIn(ExperimentalUnsignedTypes::class)
		fun mapPublicKeyIcc(encodedPk: GeneralAuthenticateResponse.MapNonce): Step3
	}

	interface Step3 {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun getEncodedPublicKeyPcd(): UByteArray

		@OptIn(ExperimentalUnsignedTypes::class)
		fun decodePublicKeyIcc(encodedPk: GeneralAuthenticateResponse.KeyAgreement): Step4
	}

	interface Step4 {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun getAuthenticationTokenPcd(): UByteArray

		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyTokenIcc(authResponse: GeneralAuthenticateResponse.AuthenticationToken): Result
	}

	interface Result {
		@OptIn(ExperimentalUnsignedTypes::class)
		val macKey: UByteArray

		@OptIn(ExperimentalUnsignedTypes::class)
		val encKey: UByteArray

		@OptIn(ExperimentalUnsignedTypes::class)
		val currentCar: UByteArray?

		@OptIn(ExperimentalUnsignedTypes::class)
		val previousCar: UByteArray?

		@OptIn(ExperimentalUnsignedTypes::class)
		val idIcc: UByteArray?
	}
}

expect fun cryptoSuite(
	paceInfos: EfCardAccess.PaceInfos,
	password: String,
): PaceCryptoSuite
