package org.openecard.sc.pace.crypto

import dev.whyoleg.cryptography.random.CryptographyRandom
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse
import kotlin.random.Random

private val log = KotlinLogging.logger { }

internal class OSslPaceCryptoSuite(
	override val paceInfos: EfCardAccess.PaceInfos,
	private val password: String,
	private val rand: Random = CryptographyRandom.Default,
) : PaceCryptoSuite,
	AutoCloseable {
	private val kdf = Kdf(paceInfos.info.kdfLength)

	val curve =
		OSslEcCurve(
			paceInfos.info.standardizedDomainParameters,
		)

	val curveToFree = mutableListOf(curve)

	override fun close() {
		curveToFree.forEach { it.close() }
	}

	override fun start(): PaceCryptoSuite.Step1 =
		Step1(
			kdf.derivePi(
				password.encodeToByteArray(),
			),
		)

	inner class Step1(
		internal val passwordKey: ByteArray,
	) : PaceCryptoSuite.Step1 {
		override fun decryptNonce(encryptedNonce: GeneralAuthenticateResponse.EncryptedNonce): PaceCryptoSuite.Step2 {
			log.debug { "decryptNonce" }
			val encryptedNonceValue = encryptedNonce.encryptedNonce.toByteArray()
			val nonce = aesCbcKey(passwordKey).cipher(false).decryptWithIv(ByteArray(16), encryptedNonceValue)

			return Step2(nonce)
		}
	}

	inner class Step2(
		internal val nonce: ByteArray,
	) : PaceCryptoSuite.Step2 {
		internal val pcdMapKey = curve.generateKeyPair(rand)

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun mapPublicKeyPcd(): UByteArray = pcdMapKey.publicKey.encoded

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun mapPublicKeyIcc(encodedPk: GeneralAuthenticateResponse.MapNonce): PaceCryptoSuite.Step3 {
			val iccMapKey = OSslEcPublicKey.decodePublicKey(encodedPk.mappingData, curve)
			// check that keys are not equal
			if (pcdMapKey.publicKey == iccMapKey) {
				log.error { "Same key used for iccMapKey and pcdMapKey" }
				throw PaceError(PaceResultCode.GA2_ERROR, null)
			} else {
				return Step3(
					pcdMapKey.privateKey.mapGeneral(iccMapKey, nonce).also {
						curveToFree.add(it)
					},
				)
			}
		}
	}

	inner class Step3(
		internal val curve: OSslEcCurve,
	) : PaceCryptoSuite.Step3 {
		internal val pcdKey = curve.generateKeyPair(rand)

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun getEncodedPublicKeyPcd(): UByteArray = pcdKey.publicKey.encoded

		@OptIn(ExperimentalUnsignedTypes::class)
		override fun decodePublicKeyIcc(encodedPk: GeneralAuthenticateResponse.KeyAgreement): PaceCryptoSuite.Step4 {
			val iccKey = OSslEcPublicKey.decodePublicKey(encodedPk.ephemeralPubKey, curve)
			if (pcdKey.publicKey == iccKey) {
				log.error { "Same key used for iccKey and pcdKey" }
				throw PaceError(PaceResultCode.GA3_ERROR, null)
			} else {
				return Step4(pcdKey, iccKey)
			}
		}
	}

	inner class Step4(
		internal val pcdKey: OSslEcKeyPair,
		internal val iccKey: OSslEcPublicKey,
	) : PaceCryptoSuite.Step4 {
		@OptIn(ExperimentalUnsignedTypes::class)
		val sharedSecret =
			pcdKey.privateKey
				.sharedSecret(iccKey)
				.x
				.toByteArray()

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
			return Result(
				macKey.toUByteArray(),
				encKey.toUByteArray(),
				authResponse.curCar,
				authResponse.prevCar,
				iccKey.encodedCompressed,
			)
		}
	}

	inner class Result
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			override val macKey: UByteArray,
			override val encKey: UByteArray,
			override val currentCar: UByteArray?,
			override val previousCar: UByteArray?,
			override val idIcc: UByteArray?,
		) : PaceCryptoSuite.Result
}
