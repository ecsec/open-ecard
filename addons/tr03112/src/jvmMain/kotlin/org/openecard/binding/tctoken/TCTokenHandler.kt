/** **************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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
package org.openecard.binding.tctoken

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ActionType
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.CreateSession
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse
import iso.std.iso_iec._24727.tech.schema.DestroySession
import org.openecard.addon.AddonManager
import org.openecard.addon.Context
import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException
import org.openecard.binding.tctoken.ex.NonGuiException
import org.openecard.binding.tctoken.ex.ResultMinor
import org.openecard.binding.tctoken.ex.SecurityViolationException
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.ECardConstants.BINDING_HTTP
import org.openecard.common.ECardConstants.BINDING_PAOS
import org.openecard.common.I18n
import org.openecard.common.OpenecardProperties
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.DispatcherException
import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.interfaces.DocumentValidatorException
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.util.FuturePromise
import org.openecard.common.util.HandlerUtils
import org.openecard.common.util.JAXPSchemaValidator
import org.openecard.common.util.Promise
import org.openecard.gui.UserConsent
import org.openecard.gui.message.DialogType
import org.openecard.httpcore.HttpResourceException
import org.openecard.httpcore.InvalidProxyException
import org.openecard.httpcore.InvalidUrlException
import org.openecard.httpcore.ValidationError
import org.openecard.transport.paos.PAOSConnectionException
import org.openecard.transport.paos.PAOSException
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.TreeSet
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.xml.transform.TransformerException

/**
 * Transport binding agnostic TCToken handler. <br></br>
 * This handler supports the following transports:
 *
 *  * PAOS
 *
 *
 *
 * This handler supports the following security protocols:
 *
 *  * TLS
 *  * TLS-PSK
 *  * PLS-PSK-RSA
 *
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */

private val LOG = KotlinLogging.logger { }

class TCTokenHandler(
	ctx: Context,
) {
	private val pin: String = LANG_PACE.translationForKey("pin")
	private val puk: String = LANG_PACE.translationForKey("puk")
	private val evtDispatcher: EventDispatcher = ctx.eventDispatcher
	private val dispatcher: Dispatcher = ctx.dispatcher
	private val gui: UserConsent = ctx.userConsent
	private val manager: AddonManager = ctx.manager
	private val schemaValidator: Promise<DocumentSchemaValidator> =
		FuturePromise<DocumentSchemaValidator>(
			Callable {
				val noValid = OpenecardProperties.getProperty("legacy.invalid_schema").toBoolean()
				if (!noValid) {
					try {
						return@Callable JAXPSchemaValidator.load("Management.xsd")
					} catch (ex: SAXException) {
						LOG.warn(ex) { "No Schema Validator available, skipping schema validation." }
					}
				}
				// always valid
				LOG.warn { "Schema validation is disabled." }
				object : DocumentSchemaValidator {
					override fun validate(doc: Document) {
					}

					override fun validate(doc: Element) {
					}
				}
			},
		)

	private fun preparePaosHandle(): ConnectionHandleType {
		// Perform a CreateSession to initialize the SAL.
		val createSession = CreateSession()
		val createSessionResp = dispatcher.safeDeliver(createSession) as CreateSessionResponse

		// Check CreateSessionResponse
		checkResult<CreateSessionResponse>(createSessionResp)

		// Update ConnectionHandle.
		val connectionHandle = createSessionResp.getConnectionHandle()

		return connectionHandle
	}

	private fun prepareTlsHandle(connectionHandle: ConnectionHandleType?): ConnectionHandleType? {
		// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
		var connectionHandle = connectionHandle
		val appPath = CardApplicationPath()
		appPath.setCardAppPathRequest(connectionHandle)
		val appPathRes = dispatcher.safeDeliver(appPath) as CardApplicationPathResponse

		// Check CardApplicationPathResponse
		checkResult<CardApplicationPathResponse>(appPathRes)

		val appConnect = CardApplicationConnect()
		val pathRes: MutableList<CardApplicationPathType> =
			appPathRes
				.getCardAppPathResultSet()
				.getCardApplicationPathResult()
		appConnect.setCardApplicationPath(pathRes[0])
		val appConnectRes = dispatcher.safeDeliver(appConnect) as CardApplicationConnectResponse
		// Update ConnectionHandle. It now includes a SlotHandle.
		connectionHandle = appConnectRes.getConnectionHandle()

		// Check CardApplicationConnectResponse
		checkResult(appConnectRes)

		return connectionHandle
	}

	/**
	 * Performs the actual PAOS procedure. Connects the given card, establishes the HTTP channel and talks to the
	 * server. Afterwards disconnects the card.
	 *
	 * @return A TCTokenResponse indicating success or failure.
	 * @throws DispatcherException If there was a problem dispatching a request from the server.
	 * @throws PAOSException If there was a transport error.
	 */
	private fun processBinding(
		ctx: Context?,
		tokenReq: TCTokenRequest,
	): TCTokenResponse {
		val token = tokenReq.tCToken
		try {
			val binding = token.getBinding()
			val taskResult: FutureTask<*>
			val taskName: String
			when (binding) {
				BINDING_PAOS -> {
					// send StartPAOS
					val connectionHandle = preparePaosHandle()
					prepareForTask(tokenReq, connectionHandle)
					val supportedDIDs = this.supportedDIDs
					val task = PAOSTask(dispatcher, connectionHandle, supportedDIDs, tokenReq, schemaValidator)
					taskResult = FutureTask(task)
					taskName = "PAOS"
				}

				BINDING_HTTP -> {
					// no actual binding, just connect via tls and authenticate the user with that connection

					// we know exactly which card we want
					// TODO: see if we need to really do this, as the handle never leaves the OeC
					val connectionHandle = preparePaosHandle()
					prepareForTask(tokenReq, connectionHandle)
					// get first handle, currently we just support one and this is likely not to change soon
					val task = HttpGetTask(dispatcher, evtDispatcher, connectionHandle, tokenReq)
					taskResult = FutureTask(task)
					taskName = "TLS Auth"
				}

				else -> // unknown binding
					throw RuntimeException("Unsupported binding in TCToken.")
			}
			val taskThread = Thread(taskResult, taskName)
			taskThread.start()
			// wait for computation to finish
			waitForTask(taskResult)

			val response = TCTokenResponse()
			response.tCToken = token
			response.setResult(makeResultOK())
			response.bindingTask = taskResult

			return response
		} catch (ex: WSHelper.WSException) {
			val msg = "Failed to connect to card."
			LOG.error(ex) { msg }

			if (ECardConstants.Minor.IFD.CANCELLATION_BY_USER == ex.resultMinor) {
				throw PAOSException(ex)
			}

			throw DispatcherException(msg, ex)
		}
	}

	/**
	 * Activates the client according to the received TCToken.
	 *
	 * @param params The parameters defining the request.
	 * @return The response containing the result of the activation process.
	 * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
	 * @throws SecurityViolationException
	 * @throws NonGuiException
	 */

	fun handleActivate(
		params: Map<String, String>,
		ctx: Context?,
	): BindingResult {
		var tokenReq: TCTokenRequest? = null
		try {
			tokenReq = TCTokenRequest.fetchTCToken(params)
			return this.handleActivateInner(ctx, tokenReq)
		} finally {
			tokenReq?.tokenContext?.closeStream()
		}
	}

	fun handleActivateInner(
		ctx: Context?,
		tokenReq: TCTokenRequest,
	): TCTokenResponse {
		val token = tokenReq.tCToken
		if (LOG.isDebugEnabled()) {
			try {
				val m = createInstance()
				LOG.debug { "${"TCToken:\n{}"} ${m.doc2str(m.marshal(token))}" }
			} catch (ex: TransformerException) {
				// it's no use
			} catch (ex: WSMarshallerException) {
			}
		}

		var response = TCTokenResponse()
		response.tCToken = token
		// TODO: make it work again according to redesign
// 	Set<CardStateEntry> matchingHandles = cardStates.getMatchingEntries(requestedHandle);
// 	if (!matchingHandles.isEmpty()) {
// 	    connectionHandle = matchingHandles.toArray(new CardStateEntry[]{})[0].handleCopy();
// 	}
//
// 	if (connectionHandle == null) {
// 	    String msg = LANG_TOKEN.translationForKey("cancel");
// 	    LOG.error(msg);
// 	    response.setResult(WSHelper.makeResultError(ResultMinor.CANCELLATION_BY_USER, msg));
// 	    // fill in values, so it is usuable by the transport module
// 	    response = determineRefreshURL(params, response);
// 	    response.finishResponse();
// 	    return response;
// 	}
		try {
			// process binding and follow redirect addresses afterwards
			response = processBinding(ctx, tokenReq)
			// fill in values, so it is usuable by the transport module
			response = determineRefreshURL(tokenReq, response)
			response.finishResponse()
			return response
		} catch (w: DispatcherException) {
			LOG.error(w) { "${w.message}" }

			response.resultCode = BindingResultCode.INTERNAL_ERROR
			response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, w.message))
			showErrorMessage(w.message)
			throw NonGuiException(response, w.message!!, w)
		} catch (w: PAOSException) {
			LOG.error(w) { "${w.message}" }

			// find actual error to display to the user
			var innerException = w.cause
			if (innerException == null) {
				innerException = w
			} else if (innerException is ExecutionException) {
				innerException = innerException.cause
			}

			var errorMsg = innerException!!.localizedMessage
			errorMsg = errorMsg ?: "" // fix NPE when null is returned instead of a message
			when (errorMsg) {
				"The target server failed to respond" ->
					errorMsg =
						LANG_TR.translationForKey(
							ErrorTranslations.NO_RESPONSE_FROM_SERVER,
						)

				ECardConstants.Minor.App.INT_ERROR + " ==> Unknown eCard exception occurred." ->
					errorMsg =
						LANG_TR.translationForKey(
							ErrorTranslations.UNKNOWN_ECARD_ERROR,
						)

				"Internal TLS error, this could be an attack" ->
					errorMsg =
						LANG_TR.translationForKey(ErrorTranslations.INTERNAL_TLS_ERROR)
			}

			LOG.debug(innerException) { "Processing InnerException." }
			when (innerException) {
				is WSHelper.WSException -> {
					val ex = innerException
					errorMsg = createResponseFromWsEx(ex, response)
				}

				is PAOSConnectionException -> {
					response.setResult(
						makeResultError(
							ResultMinor.TRUSTED_CHANNEL_ESTABLISHMENT_FAILED,
							w.getLocalizedMessage(),
						),
					)
					response.setAdditionalResultMinor(ECardConstants.Minor.Disp.COMM_ERROR)
				}

				is InterruptedException -> {
					response.resultCode = BindingResultCode.INTERRUPTED
					response.setResult(makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg))
					response.setAdditionalResultMinor(ECardConstants.Minor.App.SESS_TERMINATED)
				}

				is DocumentValidatorException -> {
					errorMsg = LANG_TR.translationForKey(ErrorTranslations.SCHEMA_VALIDATION_FAILED)
					// it is ridiculous, that this should be a client error, but the test spec demands this
					response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, w.message))
					response.setAdditionalResultMinor(ECardConstants.Minor.SAL.Support.SCHEMA_VAILD_FAILED)
				}

				else -> {
					errorMsg = createMessageFromUnknownError(w)
					response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, w.message))
					response.setAdditionalResultMinor(ECardConstants.Minor.App.UNKNOWN_ERROR)
				}
			}

			val paosAdditionalMinor = w.additionalResultMinor
			if (paosAdditionalMinor != null) {
				LOG.debug { "Replacing minor from inner exception with minor from PAOSException." }
				LOG.debug { "${"InnerException minor: {}"} ${response.auxResultData.get(AuxDataKeys.MINOR_PROCESS_RESULT)}" }
				LOG.debug { "${"PAOSException minor: {}"} $paosAdditionalMinor" }
				response.setAdditionalResultMinor(paosAdditionalMinor)
			}

			showErrorMessage(errorMsg)

			try {
				// fill in values, so it is usuable by the transport module
				response = determineRefreshURL(tokenReq, response)
				response.finishResponse()
			} catch (ex: InvalidRedirectUrlException) {
				LOG.error(ex) { "${ex.message}" }
				// in case we were interrupted before, use INTERRUPTED as result status
				if (innerException is InterruptedException) {
					response.resultCode = BindingResultCode.INTERRUPTED
					response.setResult(makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg))
				} else {
					response.resultCode = BindingResultCode.INTERNAL_ERROR
					response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, ex.getLocalizedMessage()))
					throw NonGuiException(response, ex.message!!, ex)
				}
			} catch (ex: SecurityViolationException) {
				val msg2 = (
					"The RefreshAddress contained in the TCToken is invalid. Redirecting to the " +
						"CommunicationErrorAddress."
				)
				LOG.error(ex) { msg2 }
				response.resultCode = BindingResultCode.REDIRECT
				response.setResult(makeResultError(ResultMinor.COMMUNICATION_ERROR, msg2))
				response.addAuxResultData(
					AuxDataKeys.REDIRECT_LOCATION,
					ex.bindingResult.auxResultData.get(
						AuxDataKeys.REDIRECT_LOCATION,
					),
				)
			}

			return response
		}
	}

	private val supportedDIDs: MutableList<String?>
		get() {
			val result = TreeSet<String?>()

			// check all sal protocols in the
			val registry = manager.getRegistry()
			val addons =
				registry.listAddons()
			for (addon in addons) {
				for (proto in addon.getSalActions()) {
					result.add(proto.getUri())
				}
			}

			return ArrayList<String?>(result)
		}

	private fun showBackgroundMessage(
		msg: String?,
		title: String?,
		dialogType: DialogType?,
	) {
		Thread(
			Runnable {
				gui.obtainMessageDialog().showMessageDialog(msg, title, dialogType)
			},
			"Background_MsgBox",
		).start()
	}

	private fun showErrorMessage(errMsg: String?) {
		val title: String? = LANG_TR.translationForKey(ErrorTranslations.ERROR_TITLE)
		val baseHeader: String? = LANG_TR.translationForKey(ErrorTranslations.ERROR_HEADER)
		val exceptionPart: String? = LANG_TR.translationForKey(ErrorTranslations.ERROR_MSG_IND)
		val removeCard: String? = LANG_TR.translationForKey(ErrorTranslations.REMOVE_CARD)
		val msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard)
		showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE)
	}

	private fun createResponseFromWsEx(
		ex: WSHelper.WSException,
		response: TCTokenResponse,
	): String? {
		val errorMsg: String?
		val minor = ex.resultMinor

		when (minor) {
			ECardConstants.Minor.Disp.TIMEOUT,
			ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
			ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
			-> {
				errorMsg = LANG_TOKEN.translationForKey("cancel")
				response.setResult(makeResultError(ResultMinor.CANCELLATION_BY_USER, errorMsg))
			}

			ECardConstants.Minor.SAL.EAC.DOC_VALID_FAILED -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.CERT_ERROR)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.App.INCORRECT_PARM -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.MESSAGE_CONTENT_INVALID)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.App.INT_ERROR -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.INTERNAL_ERROR)
				response.setResult(makeResultError(ResultMinor.SERVER_ERROR, errorMsg))
			}

			ECardConstants.Minor.SAL.PREREQUISITES_NOT_SATISFIED -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.CERT_DESCRIPTION_CHECK_FAILED)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.App.UNKNOWN_ERROR -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.ERROR_WHILE_AUTHENTICATION)
				response.setResult(makeResultError(ResultMinor.SERVER_ERROR, errorMsg))
			}

			ECardConstants.Minor.SAL.UNKNOWN_HANDLE -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.UNKNOWN_CONNECTION_HANDLE)
				response.setResult(makeResultError(ResultMinor.SERVER_ERROR, errorMsg))
			}

			ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE -> {
				errorMsg = LANG_PIN.translationForKey(ERROR_CARD_REMOVED)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.IFD.PASSWORD_BLOCKED -> {
				errorMsg = LANG_PACE.translationForKey("step_error_pin_blocked", pin, pin, puk, pin)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED -> {
				errorMsg = LANG_PACE.translationForKey("step_error_pin_deactivated")
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			ECardConstants.Minor.IFD.UNKNOWN_ERROR -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.ERROR_WHILE_AUTHENTICATION)
				response.setResult(makeResultError(ResultMinor.CLIENT_ERROR, errorMsg))
			}

			else -> {
				errorMsg = LANG_TR.translationForKey(ErrorTranslations.ERROR_WHILE_AUTHENTICATION)
				response.setResult(makeResultError(ResultMinor.SERVER_ERROR, errorMsg))
			}
		}

		response.setAdditionalResultMinor(minor)

		return errorMsg
	}

	/**
	 * Creates an error message from an PAOSException which contains a not handled inner exception.
	 *
	 * @param w An PAOSException containing a not handled inner exception.
	 * @return A sting containing an error message.
	 */
	private fun createMessageFromUnknownError(w: PAOSException): String {
		var errorMsg = "\n"
		errorMsg += LANG_TR.translationForKey(ErrorTranslations.UNHANDLED_INNER_EXCEPTION)
		errorMsg += "\n"
		errorMsg += w.message
		return errorMsg
	}

	companion object {
		private val LANG_TR: I18n = I18n.getTranslation("tr03112")
		private val LANG_TOKEN: I18n = I18n.getTranslation("tctoken")
		private val LANG_PIN: I18n = I18n.getTranslation("pinplugin")
		private val LANG_PACE: I18n = I18n.getTranslation("pace")

		// Translation constants
		private const val ERROR_CARD_REMOVED = "action.error.card.removed"

		fun disconnectHandle(
			dispatcher: Dispatcher,
			connectionHandle: ConnectionHandleType?,
		) {
			// disconnect card after authentication
			val appDis = CardApplicationDisconnect()
			appDis.setConnectionHandle(connectionHandle)
			appDis.setAction(ActionType.RESET)
			dispatcher.safeDeliver(appDis)
		}

		// TODO: check where this has been used and if it is still needed
		fun destroySession(
			dispatcher: Dispatcher,
			connectionHandle: ConnectionHandleType?,
		) {
			val destroySession = DestroySession()
			destroySession.setConnectionHandle(connectionHandle)
			dispatcher.safeDeliver(destroySession)
		}

		private fun prepareForTask(
			request: TCTokenRequest,
			connectionHandle: ConnectionHandleType?,
		) {
			val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
			val performChecks = request.isPerformTR03112Checks
			if (!performChecks) {
				LOG.warn { "Checks according to BSI TR03112 3.4.2, 3.4.4 (TCToken specific) and 3.4.5 are disabled." }
			}
			dynCtx.put(TR03112Keys.ACTIVATION_THREAD, Thread.currentThread())
			dynCtx.put(TR03112Keys.TCTOKEN_CHECKS, performChecks)
			dynCtx.put(TR03112Keys.TCTOKEN_SERVER_CERTIFICATES, request.certificates)

			dynCtx.put(TR03112Keys.SESSION_CON_HANDLE, HandlerUtils.copyHandle(connectionHandle))
		}

		private fun waitForTask(task: Future<*>) {
			try {
				task.get()
			} catch (ex: InterruptedException) {
				LOG.info { "Waiting for PAOS Task to finish has been interrupted. Cancelling authentication." }
				task.cancel(true)
				throw PAOSException(ex)
			} catch (ex: ExecutionException) {
				LOG.warn(ex) { "The result of PAOS Task could not be retieved." }
				// perform conversion of ExecutionException from the Future to the really expected exceptions
				when (ex.cause) {
					is PAOSException -> {
						throw ex.cause as PAOSException
					}
					is DispatcherException -> {
						throw ex.cause as DispatcherException
					}
					else -> {
						throw PAOSException(ex)
					}
				}
			}
		}

		/**
		 * Follow the URL in the RefreshAddress and update it in the response. The redirect is followed as long as the
		 * response is a redirect (302, 303 or 307) AND is a https-URL AND the hash of the retrieved server certificate is
		 * contained in the CertificateDescrioption, else return 400. If the URL and the subjectURL in the
		 * CertificateDescription conform to the SOP we reached our final destination.
		 *
		 * @param request TCToken request used to determine which security checks to perform.
		 * @param response The TCToken response in which the original refresh address is defined and where it will be
		 * updated.
		 * @return Modified response with the final address the browser should be redirected to.
		 * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
		 */
		private fun determineRefreshURL(
			request: TCTokenRequest,
			response: TCTokenResponse,
		): TCTokenResponse {
			try {
				val endpointStr = response.refreshAddress
				var endpoint = URL(endpointStr)
				val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

				// disable certificate checks according to BSI TR03112-7 in some situations
				val redirectChecks = request.isPerformTR03112Checks
				val verifier = RedirectCertificateValidator(redirectChecks)
				val ctx = TrResourceContextLoader().getStream(endpoint, verifier)
				ctx!!.closeStream()

				// using this verifier no result must be present, meaning no status code different than a redirect occurred
// 	    if (result.p1 != null) {
// 		// TODO: this error is expected according the spec, handle it in a different way
// 		String msg = "Return-To-Websession yielded a non-redirect response.";
// 		throw new IOException(msg);
// 	    }
				// determine redirect
				val resultPoints = ctx.certs
				val last = resultPoints[resultPoints.size - 1]
				endpoint = last.p1
				dynCtx.put(TR03112Keys.IS_REFRESH_URL_VALID, true)
				LOG.debug { "${"Setting redirect address to '{}'."} $endpoint" }
				response.refreshAddress = endpoint.toString()
				return response
			} catch (ex: MalformedURLException) {
				throw IllegalStateException(LANG_TR.translationForKey(ErrorTranslations.REFRESH_URL_ERROR), ex)
			} catch (ex: HttpResourceException) {
				val code = ECardConstants.Minor.App.COMMUNICATION_ERROR
				val communicationErrorAddress = response.tCToken.getComErrorAddressWithParams(code)

				if (communicationErrorAddress.isNotEmpty()) {
					throw SecurityViolationException(
						communicationErrorAddress,
						ErrorTranslations.REFRESH_DETERMINATION_FAILED,
						ex,
					)
				}
				throw InvalidRedirectUrlException(ErrorTranslations.REFRESH_DETERMINATION_FAILED, ex)
			} catch (ex: InvalidUrlException) {
				val code = ECardConstants.Minor.App.COMMUNICATION_ERROR
				val communicationErrorAddress = response.tCToken.getComErrorAddressWithParams(code)

				if (communicationErrorAddress.isNotEmpty()) {
					throw SecurityViolationException(
						communicationErrorAddress,
						ErrorTranslations.REFRESH_DETERMINATION_FAILED,
						ex,
					)
				}
				throw InvalidRedirectUrlException(ErrorTranslations.REFRESH_DETERMINATION_FAILED, ex)
			} catch (ex: InvalidProxyException) {
				val code = ECardConstants.Minor.App.COMMUNICATION_ERROR
				val communicationErrorAddress = response.tCToken.getComErrorAddressWithParams(code)

				if (communicationErrorAddress.isNotEmpty()) {
					throw SecurityViolationException(
						communicationErrorAddress,
						ErrorTranslations.REFRESH_DETERMINATION_FAILED,
						ex,
					)
				}
				throw InvalidRedirectUrlException(ErrorTranslations.REFRESH_DETERMINATION_FAILED, ex)
			} catch (ex: ValidationError) {
				val code = ECardConstants.Minor.App.COMMUNICATION_ERROR
				val communicationErrorAddress = response.tCToken.getComErrorAddressWithParams(code)

				if (communicationErrorAddress.isNotEmpty()) {
					throw SecurityViolationException(
						communicationErrorAddress,
						ErrorTranslations.REFRESH_DETERMINATION_FAILED,
						ex,
					)
				}
				throw InvalidRedirectUrlException(ErrorTranslations.REFRESH_DETERMINATION_FAILED, ex)
			} catch (ex: IOException) {
				val code = ECardConstants.Minor.App.COMMUNICATION_ERROR
				val communicationErrorAddress = response.tCToken.getComErrorAddressWithParams(code)

				if (communicationErrorAddress.isNotEmpty()) {
					throw SecurityViolationException(
						communicationErrorAddress,
						ErrorTranslations.REFRESH_DETERMINATION_FAILED,
						ex,
					)
				}
				throw InvalidRedirectUrlException(ErrorTranslations.REFRESH_DETERMINATION_FAILED, ex)
			}
		}
	}
}
