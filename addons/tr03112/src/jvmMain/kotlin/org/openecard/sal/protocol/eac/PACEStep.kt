/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.sal.protocol.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.addon.Context
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.ECardException
import org.openecard.common.I18n
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.ifd.anytype.PACEOutputType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.DispatcherException
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.util.Pair
import org.openecard.common.util.TR03112Utils
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.crypto.common.asn1.cvc.CHATVerifier.verfiy
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateVerifier
import org.openecard.crypto.common.asn1.cvc.CertificateDescription
import org.openecard.crypto.common.asn1.eac.AuthenticatedAuxiliaryData
import org.openecard.crypto.common.asn1.eac.SecurityInfos
import org.openecard.gui.ResultStatus
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.sal.protocol.eac.anytype.EAC1InputType
import org.openecard.sal.protocol.eac.anytype.ElementParsingException
import org.openecard.sal.protocol.eac.gui.CHATStep
import org.openecard.sal.protocol.eac.gui.CHATStepAction
import org.openecard.sal.protocol.eac.gui.CVCStep
import org.openecard.sal.protocol.eac.gui.CVCStepAction
import org.openecard.sal.protocol.eac.gui.PINStep
import org.openecard.sal.protocol.eac.gui.ProcessingStep
import org.openecard.sal.protocol.eac.gui.ProcessingStepAction
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URL
import java.security.cert.CertificateException
import kotlin.Any
import kotlin.Exception
import kotlin.RuntimeException
import kotlin.String
import kotlin.Throwable
import kotlin.toString

private val logger = KotlinLogging.logger { }

private val LANG: I18n = I18n.getTranslation("eac")
private val LANG_PACE: I18n = I18n.getTranslation("pace")

// GUI translation constants
private const val TITLE = "eac_user_consent_title"

/**
 * Implements PACE protocol step according to BSI TR-03112-7.
 *
 * @see "BSI-TR-03112, version 1.1.2., part 7, section 4.6.5."
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class PACEStep(
	private val addonCtx: Context,
) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	private val dispatcher: Dispatcher = addonCtx.dispatcher
	private val gui: UserConsent = addonCtx.userConsent
	private val eventDispatcher: EventDispatcher = addonCtx.eventDispatcher

	override fun getFunctionType(): FunctionType = FunctionType.DIDAuthenticate

	override fun perform(
		request: DIDAuthenticate,
		internalData: MutableMap<String, Any?>,
	): DIDAuthenticateResponse {
		// get context to save values in
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		val didAuthenticate = request
		val response: DIDAuthenticateResponse =
			WSHelper.makeResponse(
				DIDAuthenticateResponse::class.java,
				WSHelper
					.makeResultOK(),
			)

		// EACProtocol.setEmptyResponseData(response);
		try {
			val eac1Input = EAC1InputType(didAuthenticate.getAuthenticationProtocolData())
			val eac1Output = eac1Input.outputType

			val aad = AuthenticatedAuxiliaryData(eac1Input.authenticatedAuxiliaryData)
			var pwId = PasswordID.PIN

			// Certificate chain
			val certChain = CardVerifiableCertificateChain(eac1Input.certificates)
			val rawCertificateDescription = eac1Input.certificateDescription!!
			val certDescription = CertificateDescription.getInstance(rawCertificateDescription)

			// put CertificateDescription into DynamicContext which is needed for later checks
			dynCtx.put(TR03112Keys.ESERVICE_CERTIFICATE_DESC, certDescription)

			// according to BSI-TR-03124-1 we MUST perform some checks immediately after receiving the eService cert
			val activationChecksResult = performChecks(certDescription, dynCtx)
			if (ECardConstants.Major.OK != activationChecksResult.getResultMajor()) {
				response.setResult(activationChecksResult)
				dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
				return response
			}

			// Verify that the certificate description matches the terminal certificate
			val taCert = certChain.terminalCertificate
			CardVerifiableCertificateVerifier.verify(taCert!!, certDescription)

			// get CHAT values
			val taCHAT = taCert.cHAT
			val requiredCHAT = CHAT(eac1Input.requiredCHAT)
			val optionalCHAT = CHAT(eac1Input.optionalCHAT)

			// Check that we got an authentication terminal terminal certificate. We abort the process in case there is
			// an other role.
			if (taCHAT.role != CHAT.Role.AUTHENTICATION_TERMINAL) {
				val msg =
					"Unsupported terminal type in Terminal Certificate referenced. Referenced terminal type is " +
						taCHAT.role.toString() + "."
				response.setResult(makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg))
				dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
				return response
			}

			// enable CAN_ALLOWED in case it is set in the cert description
			if (taCHAT.getSpecialFunctions().get(CHAT.SpecialFunction.CAN_ALLOWED) == true) {
				requiredCHAT.setSpecialFunctions(CHAT.SpecialFunction.CAN_ALLOWED, true)
				optionalCHAT.setSpecialFunctions(CHAT.SpecialFunction.CAN_ALLOWED, true)
				pwId = PasswordID.CAN
			}

			// verify that required chat does not contain any prohibited values
			verfiy(taCHAT, requiredCHAT)

			// remove overlapping values from optional chat
			optionalCHAT.restrictAccessRights(taCHAT)

			// Prepare data in DIDAuthenticate for GUI
			val pinID = pwId.byte
			val eacData = EACData()
			eacData.didRequest = didAuthenticate
			eacData.certificate = certChain.terminalCertificate!!
			eacData.certificateDescription = certDescription
			eacData.rawCertificateDescription = rawCertificateDescription
			val transactionInfo = eac1Input.transactionInfo
			eacData.transactionInfo = transactionInfo
			// remove transaction info again if it is empty
			if (transactionInfo != null && transactionInfo.trim { it <= ' ' }.isEmpty()) {
				eacData.transactionInfo = null
			}
			eacData.requiredCHAT = requiredCHAT
			eacData.optionalCHAT = optionalCHAT
			eacData.selectedCHAT = CHAT(requiredCHAT.toByteArray())
			eacData.aad = aad
			eacData.pinID = pinID
			dynCtx.put(EACProtocol.Companion.EAC_DATA, eacData)

			// define GUI depending on the PIN status
			val uc = UserConsentDescription(LANG.translationForKey(TITLE))
			uc.setDialogType("EAC")

			// create GUI and init executor
			val cvcStep = CVCStep(eacData)
			val cvcStepAction = CVCStepAction(cvcStep)
			cvcStep.setAction(cvcStepAction)
			uc.getSteps().add(cvcStep)

			val chatStep = CHATStep(eacData)
			val chatAction = CHATStepAction(addonCtx, eacData, chatStep)
			chatStep.setAction(chatAction)
			uc.getSteps().add(chatStep)

			uc.getSteps().add(PINStep.createDummy(pinID))
			val procStep = ProcessingStep()
			val procStepAction = ProcessingStepAction(procStep)
			procStep.setAction(procStepAction)
			uc.getSteps().add(procStep)

			val guiThread =
				Thread(
					Runnable {
						// get context here because it is thread local
						val dynCtx2 = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
						if (!uc.getSteps().isEmpty()) {
							val navigator = gui.obtainNavigator(uc)
							val exec = ExecutionEngine(navigator)
							var guiResult: ResultStatus?
							try {
								guiResult = exec.process()
							} catch (ex: ThreadTerminateException) {
								logger.debug { "GUI executer has been terminated." }
								guiResult = ResultStatus.INTERRUPTED
							}

							if (guiResult == ResultStatus.CANCEL || guiResult == ResultStatus.INTERRUPTED) {
								logger.debug { "EAC GUI returned with CANCEL or INTERRUPTED." }
								dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
								val paceErrorPromise = dynCtx2.getPromise(EACProtocol.Companion.PACE_EXCEPTION)
								val paceError = paceErrorPromise.derefNonblocking()
								if (!paceErrorPromise.isDelivered()) {
									logger.debug { "Setting PACE result to cancelled." }
									paceErrorPromise.deliver(
										createException(
											makeResultError(
												ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
												"User canceled the PACE dialog.",
											),
										),
									)
								} else {
									// determine if the error is cancel, or something else
									var needsTermination = false
									if (paceError is WSHelper.WSException) {
										val ex = paceError
										val minor = ex.resultMinor
										when (minor) {
											ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
											ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
											ECardConstants.Minor.Disp.TIMEOUT,
											->
												needsTermination =
													true
										}
									}
									// terminate activation thread if it has not been interrupted already
									if (needsTermination && guiResult != ResultStatus.INTERRUPTED) {
										val actThread = dynCtx2.get(TR03112Keys.ACTIVATION_THREAD) as Thread?
										if (actThread != null) {
											logger.debug { "Interrupting activation thread." }
											actThread.interrupt()
										}
									}
								}
							}
						}
					},
					"EAC-GUI",
				)
			dynCtx.put(TR03112Keys.OPEN_USER_CONSENT_THREAD, guiThread)
			guiThread.start()

			// wait for PACE to finish
			val pPaceException = dynCtx.getPromise(EACProtocol.Companion.PACE_EXCEPTION)
			val pPaceError = pPaceException.deref()
			if (pPaceError != null) {
				if (logger.isDebugEnabled()) {
					if (pPaceError is Throwable) {
						logger.debug(pPaceError) { "Received error object from GUI." }
					} else {
						logger.debug { "Received error object from GUI: $pPaceError" }
					}
				}

				if (pPaceError is WSHelper.WSException) {
					response.setResult(pPaceError.result)
					return response
				} else if (pPaceError is DispatcherException || pPaceError is InvocationTargetException) {
					val msg = "Internal error while PACE authentication."
					val r = makeResultError(ECardConstants.Minor.App.INT_ERROR, msg)
					response.setResult(r)
					return response
				} else {
					val msg = "Unknown error while PACE authentication."
					val r = makeResultError(ECardConstants.Minor.App.UNKNOWN_ERROR, msg)
					response.setResult(r)
					return response
				}
			} else {
				logger.debug { "No error returned returned during PACE execution in GUI." }
			}

			// get challenge from card
			val conHandle = dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType?
			if (conHandle!!.getSlotHandle() == null) {
				throw RuntimeException(
					"The connection handle stored in TR03112Keys.CONNECTION_HANDLE does not have a slot handle!",
				)
			}
			val ta = TerminalAuthentication(dispatcher, conHandle.getSlotHandle())
			val challenge = ta.challenge

			// prepare DIDAuthenticationResponse
			val data = eacData.paceResponse!!.getAuthenticationProtocolData()
			val paceOutputMap = AuthDataMap(data)

			// int retryCounter = Integer.valueOf(paceOutputMap.getContentAsString(PACEOutputType.RETRY_COUNTER));
			val efCardAccess = paceOutputMap.getContentAsBytes(PACEOutputType.EF_CARD_ACCESS)
			val currentCAR = paceOutputMap.getContentAsBytes(PACEOutputType.CURRENT_CAR)
			val previousCAR = paceOutputMap.getContentAsBytes(PACEOutputType.PREVIOUS_CAR)
			val idpicc = paceOutputMap.getContentAsBytes(PACEOutputType.ID_PICC)

			// Store SecurityInfos
			val securityInfos = SecurityInfos.getInstance(efCardAccess)
			internalData.put(EACConstants.IDATA_SECURITY_INFOS, securityInfos)
			// Store additional data
			internalData.put(EACConstants.IDATA_AUTHENTICATED_AUXILIARY_DATA, aad)
			internalData.put(EACConstants.IDATA_CERTIFICATES, certChain)
			internalData.put(EACConstants.IDATA_CURRENT_CAR, currentCAR)
			internalData.put(EACConstants.IDATA_PREVIOUS_CAR, previousCAR)
			internalData.put(EACConstants.IDATA_CHALLENGE, challenge)

			// Create response
			// eac1Output.setRetryCounter(retryCounter);
			eac1Output.setCHAT(eacData.selectedCHAT.toByteArray())
			eac1Output.setCurrentCAR(currentCAR)
			eac1Output.setPreviousCAR(previousCAR)
			eac1Output.setEFCardAccess(efCardAccess)
			eac1Output.setIDPICC(idpicc)
			eac1Output.setChallenge(challenge)

			response.setResult(makeResultOK())
			response.setAuthenticationProtocolData(eac1Output.authDataType)
		} catch (ex: CertificateException) {
			logger.error(ex) { "${ex.message}" }
			val msg = ex.message
			response.setResult(makeResultError(ECardConstants.Minor.SAL.EAC.DOC_VALID_FAILED, msg))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		} catch (e: ECardException) {
			logger.error(e) { e.message }
			response.setResult(e.result)
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		} catch (ex: ElementParsingException) {
			logger.error(ex) { "${ex.message}" }
			response.setResult(makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, ex.message))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		} catch (e: InterruptedException) {
			logger.warn(e) { "${e.message}" }
			response.setResult(makeResultUnknownError(e.message))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
			val guiThread = dynCtx.get(TR03112Keys.OPEN_USER_CONSENT_THREAD) as Thread?
			guiThread?.interrupt()
		} catch (e: Exception) {
			logger.error(e) { "${e.message}" }
			response.setResult(makeResultUnknownError(e.message))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		}

		return response
	}

	private fun convertToBoolean(o: Any?): Boolean {
		if (o is Boolean) {
			return o
		} else {
			return false
		}
	}

	/**
	 * Perform all checks as described in BSI TR-03112-7 3.4.4.
	 *
	 * @param certDescription CertificateDescription of the eService CV Certificate
	 * @param dynCtx Dynamic Context
	 * @return a [Result] set according to the results of the checks
	 */
	private fun performChecks(
		certDescription: CertificateDescription,
		dynCtx: DynamicContext,
	): Result {
		val tokenChecks = dynCtx.get(TR03112Keys.TCTOKEN_CHECKS)
		// omit these checks if explicitly disabled
		if (convertToBoolean(tokenChecks)) {
			var checkPassed = checkEidServerCertificate(certDescription, dynCtx)
			if (!checkPassed) {
				val msg = "Hash of eID-Server certificate is NOT contained in the CertificateDescription."
				// TODO check for the correct minor type
				val r = makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg)
				return r
			}

			// perform checks according to TR-03124
			checkPassed = checkTCTokenServerCertificates(certDescription, dynCtx)
			if (!checkPassed) {
				val msg = "Hash of the TCToken server certificate is NOT contained in the CertificateDescription."
				// TODO check for the correct minor type
				val r = makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg)
				return r
			}

			checkPassed = checkTCTokenAndSubjectURL(certDescription, dynCtx)
			if (!checkPassed) {
				val msg = "TCToken does not come from the server to which the authorization certificate was issued."
				// TODO check for the correct minor type
				val r = makeResultError(ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED, msg)
				return r
			}
		} else {
			logger.warn { "Checks according to BSI TR03112 3.4.4 skipped." }
		}

		// all checks passed
		return makeResultOK()
	}

	private fun checkTCTokenAndSubjectURL(
		certDescription: CertificateDescription,
		dynCtx: DynamicContext,
	): Boolean {
		val tcTokenURL = dynCtx.get(TR03112Keys.TCTOKEN_URL) as URL?
		if (tcTokenURL != null) {
			try {
				val subjectURL = URL(certDescription.subjectURL)
				return TR03112Utils.checkSameOriginPolicy(tcTokenURL, subjectURL)
			} catch (e: MalformedURLException) {
				logger.error { "SubjectURL in CertificateDescription is not a well formed URL." }
				return false
			}
		} else {
			logger.error { "No TC Token URL set in Dynamic Context." }
			return false
		}
	}

	private fun checkEidServerCertificate(
		certDescription: CertificateDescription,
		dynCtx: DynamicContext,
	): Boolean {
		val sameChannel = dynCtx.get(TR03112Keys.SAME_CHANNEL) as Boolean?
		if (true == sameChannel) {
			logger.debug { "eID-Server certificate is not check explicitly due to attached eID-Server case." }
			return true
		} else {
			val certificate = dynCtx.get(TR03112Keys.EIDSERVER_CERTIFICATE) as TlsServerCertificate?
			if (certificate != null) {
				return TR03112Utils.isInCommCertificates(
					certificate,
					certDescription.getCommCertificates(),
					"eID-Server",
				)
			} else {
				logger.error { "No eID-Server TLS Certificate set in Dynamic Context." }
				return false
			}
		}
	}

	private fun checkTCTokenServerCertificates(
		certDescription: CertificateDescription,
		dynCtx: DynamicContext,
	): Boolean {
		val certificates: List<*>? =
			dynCtx.get(TR03112Keys.TCTOKEN_SERVER_CERTIFICATES) as? List<*>
		if (certificates != null) {
			for (cert in certificates) {
				if (cert is Pair<*, *>) {
					val u = cert.p1
					val bcCert = cert.p2
					if (u is URL && bcCert is TlsServerCertificate) {
						val host =
							u.protocol + "://" + u.host + (if (u.port == -1) "" else (":" + u.port))
						if (!TR03112Utils.isInCommCertificates(bcCert, certDescription.getCommCertificates(), host)) {
							return false
						}
					}
				}
			}
			return true
		} else {
			logger.error { "No TC Token server certificates set in Dynamic Context." }
			return false
		}
	}
}
