package org.openecard.sc.pace

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.StatusWordResult
import org.openecard.sc.apdu.command.GeneralAuthenticate
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.feature.PaceResultCode
import org.openecard.sc.pace.apdu.paceMseSetAt
import org.openecard.sc.pace.asn1.EfCardAccess
import org.openecard.sc.pace.asn1.GeneralAuthenticateCommandTags
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse.toAuthenticationToken
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse.toEncryptedNonce
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse.toKeyAgreement
import org.openecard.sc.pace.asn1.GeneralAuthenticateResponse.toMapNonce
import org.openecard.sc.pace.crypto.PaceCryptoSuite
import org.openecard.sc.pace.crypto.cryptoSuite
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

class PaceProcess
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val paceInfos: EfCardAccess.PaceInfos,
		private val channel: CardChannel,
		private val pinId: PacePinId,
		private val password: String,
		private val chat: UByteArray?,
		private val certDesc: UByteArray?,
	) {
		class ProcessResult
			@OptIn(ExperimentalUnsignedTypes::class)
			constructor(
				val mseStatus: StatusWordResult,
				val macKey: UByteArray,
				val encKey: UByteArray,
				val currentCar: UByteArray?,
				val previousCar: UByteArray?,
				val idIcc: UByteArray?,
			)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun execute(): ProcessResult {
			try {
				val cryptoSuite = cryptoSuite(paceInfos, password)

				val mseStatus = mseSetAt()
				val step1 = cryptoSuite.start()
				val step2 = generalAuthenticateEncryptedNonce(step1)
				val step3 = generalAuthenticateMapNonce(step2)
				val step4 = generalAuthenticateKeyAgreement(step3)
				val cryptoResult = generalAuthenticateMutualAuthentication(step4)

				return ProcessResult(
					mseStatus,
					cryptoResult.macKey,
					cryptoResult.encKey,
					cryptoResult.currentCar,
					cryptoResult.previousCar,
					cryptoResult.idIcc,
				)
			} catch (ex: TlvException) {
				log.warn(ex) { "Invalid TLV response from card" }
				throw PaceError(PaceResultCode.TLV_RESPONSE_SYNTAX, null)
			}
		}

		/**
		 * Step 0: Initialise PACE.
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		private fun mseSetAt(): StatusWordResult {
			val mse = paceMseSetAt(paceInfos, pinId, chat, certDesc)
			val secResp = mse.transmit(channel)
			return when (secResp) {
				is SecurityCommandSuccess -> {
					secResp.status
				}
				is SecurityCommandFailure -> {
					val retries = secResp.retries
					if (retries != null && retries > 0) {
						secResp.status
					} else {
						// terminate with error
						throw PaceError(PaceResultCode.MSE_SET_AT_ERROR, secResp.status, secResp)
					}
				}
			}
		}

		/**
		 * Step 1: Encrypted nonce
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		private fun generalAuthenticateEncryptedNonce(cryptoStep: PaceCryptoSuite.Step1): PaceCryptoSuite.Step2 {
			val ga = GeneralAuthenticate.withData().setCommandChaining(true)
			val gaRes = ga.transmit(channel)
			return when (gaRes) {
				is SecurityCommandSuccess -> cryptoStep.decryptNonce(gaRes.response.toEncryptedNonce())
				is SecurityCommandFailure -> throw PaceError(PaceResultCode.GA1_ERROR, gaRes.status)
			}
		}

		/**
		 * Step 2: Mapping nonce
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		private fun generalAuthenticateMapNonce(cryptoStep: PaceCryptoSuite.Step2): PaceCryptoSuite.Step3 {
			val mapPcdDo = TlvPrimitive(GeneralAuthenticateCommandTags.mappingData, cryptoStep.mapPublicKeyPcd().toPrintable())
			val gaMapNonce = GeneralAuthenticate.withData(listOf(mapPcdDo)).setCommandChaining(true)
			val gaRes = gaMapNonce.transmit(channel)
			return when (gaRes) {
				is SecurityCommandSuccess -> cryptoStep.mapPublicKeyIcc(gaRes.response.toMapNonce())
				is SecurityCommandFailure -> throw PaceError(PaceResultCode.GA2_ERROR, gaRes.status)
			}
		}

		/**
		 * Step 3: Key agreement
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		private fun generalAuthenticateKeyAgreement(cryptoStep: PaceCryptoSuite.Step3): PaceCryptoSuite.Step4 {
			val pkPcdDo =
				TlvPrimitive(GeneralAuthenticateCommandTags.ephemeralPublicKey, cryptoStep.getEncodedPublicKeyPcd().toPrintable())
			val gaKeyAgree = GeneralAuthenticate.withData(listOf(pkPcdDo)).setCommandChaining(true)
			val gaRes = gaKeyAgree.transmit(channel)
			return when (gaRes) {
				is SecurityCommandSuccess -> cryptoStep.decodePublicKeyIcc(gaRes.response.toKeyAgreement())
				is SecurityCommandFailure -> throw PaceError(PaceResultCode.GA3_ERROR, gaRes.status)
			}
		}

		/**
		 * Step 4: Mutual authentication
		 */
		@OptIn(ExperimentalUnsignedTypes::class)
		private fun generalAuthenticateMutualAuthentication(cryptoStep: PaceCryptoSuite.Step4): PaceCryptoSuite.Result {
			val atPcdDo =
				TlvPrimitive(
					GeneralAuthenticateCommandTags.authenticationToken,
					cryptoStep.getAuthenticationTokenPcd().toPrintable(),
				)
			val gaToken = GeneralAuthenticate.withData(listOf(atPcdDo))
			val gaRes = gaToken.transmit(channel)
			return when (gaRes) {
				is SecurityCommandSuccess -> cryptoStep.verifyTokenIcc(gaRes.response.toAuthenticationToken())
				is SecurityCommandFailure -> throw PaceError(PaceResultCode.GA4_ERROR, gaRes.status)
			}
		}
	}
