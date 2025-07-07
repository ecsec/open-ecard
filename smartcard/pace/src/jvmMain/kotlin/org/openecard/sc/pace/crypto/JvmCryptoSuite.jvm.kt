package org.openecard.sc.pace.crypto

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bouncycastle.crypto.params.ECDomainParameters
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse
import org.openecard.sc.pace.crypto.BcDomainParameterResolver.resolveDomainParameters
import org.openecard.utils.common.removeLeadingZeros
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger { }

actual fun cryptoSuite(
	paceInfos: EfCardAccess.PaceInfos,
	password: String,
): PaceCryptoSuite = JvmPaceCryptoSuite(paceInfos, password)

internal class JvmPaceCryptoSuite(
	override val paceInfos: EfCardAccess.PaceInfos,
	private val password: String,
) : PaceCryptoSuite {
	private val kdf = Kdf(paceInfos.info.kdfLength)

	override fun start(): PaceCryptoSuite.Step1 {
		// derive password key
		val passwordKey = kdf.derivePi(password.toByteArray(charset = StandardCharsets.ISO_8859_1))

		return JvmStep1(passwordKey)
	}

	inner class JvmStep1(
		internal val passwordKey: ByteArray,
	) : PaceCryptoSuite.Step1 {
		@OptIn(ExperimentalUnsignedTypes::class)
		override fun decryptNonce(encryptedNonce: GeneralAuthenticateResponse.EncryptedNonce): PaceCryptoSuite.Step2 {
			val encryptedNonceValue = encryptedNonce.encryptedNonce.toByteArray()
			val nonce = aesCbcKey(passwordKey).cipher(false).decryptWithIv(ByteArray(16), encryptedNonceValue)

			return JvmStep2(nonce)
		}
	}

	inner class JvmStep2(
		internal val nonce: ByteArray,
	) : PaceCryptoSuite.Step2 {
		val defaultParams = paceInfos.info.standardizedDomainParameters.resolveDomainParameters()
		internal val pcdMapKey: BcEcKeyPair = BcEcKeyPair.generateKeyPair(defaultParams)

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun mapPublicKeyPcd(): UByteArray = pcdMapKey.publicKey.encoded

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun mapPublicKeyIcc(encodedPk: GeneralAuthenticateResponse.MapNonce): PaceCryptoSuite.Step3 {
			val iccMapKey = BcEcKeyPair.decodePublicKey(defaultParams, encodedPk.mappingData)
			// check that keys are not equal
			if (pcdMapKey.publicKey == iccMapKey) {
				log.error { "Same key used for iccMapKey and pcdMapKey" }
				throw PaceError(PaceResultCode.GA2_ERROR, null)
			} else {
				val mappedDomainParameters = pcdMapKey.privateKey.mapGeneral(iccMapKey, nonce)
				return JvmStep3(mappedDomainParameters)
			}
		}
	}

	inner class JvmStep3(
		internal val mappedDomainParameters: ECDomainParameters,
	) : PaceCryptoSuite.Step3 {
		internal val pcdKey = BcEcKeyPair.generateKeyPair(mappedDomainParameters)

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun getEncodedPublicKeyPcd(): UByteArray = pcdKey.publicKey.encoded

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun decodePublicKeyIcc(encodedPk: GeneralAuthenticateResponse.KeyAgreement): PaceCryptoSuite.Step4 {
			val iccKey = BcEcKeyPair.decodePublicKey(mappedDomainParameters, encodedPk.ephemeralPubKey)
			if (pcdKey.publicKey == iccKey) {
				log.error { "Same key used for iccKey and pcdKey" }
				throw PaceError(PaceResultCode.GA3_ERROR, null)
			} else {
				return JvmStep4(pcdKey, iccKey)
			}
		}
	}

	inner class JvmStep4(
		internal val pcdKey: BcEcKeyPair,
		internal val iccKey: BcEcKeyPair.BcEcKeyPublic,
	) : PaceCryptoSuite.Step4 {
		// TODO: check if cut leading null bytes is necessary
		@OptIn(ExperimentalUnsignedTypes::class)
		val sharedSecret = BcEcdh.generateSharedSecret(pcdKey.privateKey, iccKey).removeLeadingZeros().toByteArray()
		val macKey = kdf.deriveMac(sharedSecret)
		val encKey = kdf.deriveEnc(sharedSecret)

		@OptIn(ExperimentalUnsignedTypes::class)
		val tokenPcd = AuthenticationToken.generate(paceInfos.info, macKey, iccKey.encoded.toByteArray())

		@OptIn(ExperimentalUnsignedTypes::class)
		val tokenIcc = AuthenticationToken.generate(paceInfos.info, macKey, pcdKey.publicKey.encoded.toByteArray())

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun getAuthenticationTokenPcd(): UByteArray = tokenPcd.tokenValue

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun verifyTokenIcc(authResponse: GeneralAuthenticateResponse.AuthenticationToken): PaceCryptoSuite.Result {
			tokenIcc.verify(authResponse)

			return JvmResult(
				macKey.toUByteArray(),
				encKey.toUByteArray(),
				authResponse.curCar,
				authResponse.prevCar,
				iccKey.encoded,
			)
		}
	}

	inner class JvmResult
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			override val macKey: UByteArray,
			override val encKey: UByteArray,
			override val currentCar: UByteArray?,
			override val previousCar: UByteArray?,
			override val idIcc: UByteArray?,
		) : PaceCryptoSuite.Result
}
