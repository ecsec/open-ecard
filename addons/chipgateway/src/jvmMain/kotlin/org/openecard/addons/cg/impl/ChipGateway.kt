/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.addons.cg.impl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule
import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect
import org.apache.http.HttpException
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jwk.JsonWebKey
import org.jose4j.lang.JoseException
import org.openecard.addon.Context
import org.openecard.addons.cg.activate.TlsConnectionHandler
import org.openecard.addons.cg.ex.AuthServerException
import org.openecard.addons.cg.ex.ChipGatewayDataError
import org.openecard.addons.cg.ex.ConnectionError
import org.openecard.addons.cg.ex.ErrorTranslations
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.ex.ParameterInvalid
import org.openecard.addons.cg.ex.PinBlocked
import org.openecard.addons.cg.ex.RemotePinException
import org.openecard.addons.cg.ex.ResultMinor
import org.openecard.addons.cg.ex.SlotHandleInvalid
import org.openecard.addons.cg.ex.VersionTooOld
import org.openecard.addons.cg.tctoken.TCToken
import org.openecard.common.AppVersion.major
import org.openecard.common.AppVersion.minor
import org.openecard.common.AppVersion.patch
import org.openecard.common.AppVersion.version
import org.openecard.common.I18n
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.SemanticVersion
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.UrlBuilder
import org.openecard.common.util.ValueGenerators.generateRandom
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.NoSuchDid
import org.openecard.crypto.common.sal.did.TokenCache
import org.openecard.gui.UserConsent
import org.openecard.gui.message.DialogType
import org.openecard.httpcore.HttpRequestHelper.setDefaultHeader
import org.openecard.httpcore.KHttpUtils.dumpHttpRequest
import org.openecard.httpcore.KHttpUtils.dumpHttpResponse
import org.openecard.httpcore.StreamHttpClientConnection
import org.openecard.ws.chipgateway.CommandType
import org.openecard.ws.chipgateway.GetCommandType
import org.openecard.ws.chipgateway.HelloRequestType
import org.openecard.ws.chipgateway.HelloResponseType
import org.openecard.ws.chipgateway.ListCertificatesRequestType
import org.openecard.ws.chipgateway.ListCertificatesResponseType
import org.openecard.ws.chipgateway.ListTokensRequestType
import org.openecard.ws.chipgateway.ListTokensResponseType
import org.openecard.ws.chipgateway.ResponseType
import org.openecard.ws.chipgateway.SignRequestType
import org.openecard.ws.chipgateway.SignResponseType
import org.openecard.ws.chipgateway.TerminateType
import java.io.IOException
import java.math.BigInteger
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.Arrays
import java.util.Date
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ChipGateway(
	handler: TlsConnectionHandler,
	private val token: TCToken,
	private val addonCtx: Context,
) {
	private val tlsHandler: TlsConnectionHandler
	private val pinKey: JsonWebKey?
	private val gui: UserConsent
	private val dispatcher: Dispatcher
	private val mapper: ObjectMapper
	private val sessionId: String?
	private val addrBuilder: UrlBuilder

	private val helloUrl: URI
	private val getCommandUrl: URI
	private val listTokensUrl: URI
	private val listCertsUrl: URI
	private val signUrl: URI
	private val terminateUrl: URI

	// connection specific values
	private val httpCtx: HttpContext = BasicHttpContext()
	private val httpExecutor = HttpRequestExecutor()
	private val reuseStrategy = DefaultConnectionReuseStrategy()
	private var conn: StreamHttpClientConnection? = null
	private var canReuse = false

	@Volatile
	private var isInterrupted = false

	private var helloReq: HelloRequestType? = null
	private var showDialogThread: Thread? = null

	private val connectedSlots: MutableList<ByteArray>
		get() =
			connectedSlots.distinctBy { byteArComparator }.toMutableList()

	private val tokenCache: TokenCache

	init {
		try {
			tlsHandler = handler
			gui = addonCtx.userConsent
			dispatcher = addonCtx.dispatcher
			sessionId = token.sessionIdentifier

			addrBuilder = UrlBuilder.fromUrl(token.getServerAddress())
			helloUrl = addrBuilder.addPathSegment("HelloRequest").build()
			getCommandUrl = addrBuilder.addPathSegment("GetCommand").build()
			listTokensUrl = addrBuilder.addPathSegment("ListTokensResponse").build()
			listCertsUrl = addrBuilder.addPathSegment("ListCertificatesResponse").build()
			signUrl = addrBuilder.addPathSegment("SignResponse").build()
			terminateUrl = addrBuilder.addPathSegment("Terminate").build()

			mapper = ObjectMapper()
			mapper.registerModule(JakartaXmlBindAnnotationModule())

			tokenCache = TokenCache(dispatcher)

			var webKey: JsonWebKey? = null
			if ("http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption" == token.pathSecurityProtocol) {
				val jwkStr = token.jWK
				try {
					webKey = JsonWebKey.Factory.newJwk(jwkStr)
				} catch (ex: JoseException) {
					LOG.error(ex) { "Failed to convert JWK." }
				}
			}
			pinKey = webKey
		} catch (ex: URISyntaxException) {
			throw InvalidTCTokenElement(ErrorTranslations.MALFORMED_URL, ex, "ServerAddress")
		}
	}

	@Throws(ConnectionError::class, InvalidRedirectUrlException::class)
	private fun openHttpStream() {
		try {
			LOG.debug { "Opening connection to ChipGateway server." }
			val handler = tlsHandler.createTlsConnection()
			conn = StreamHttpClientConnection(handler.inputStream, handler.outputStream)
			LOG.debug { "Connection to ChipGateway server established." }
		} catch (ex: IOException) {
			throw ConnectionError(
				token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
				ErrorTranslations.CONNECTION_OPEN_FAILED,
				ex,
			)
		} catch (ex: URISyntaxException) {
			throw ConnectionError(
				token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
				ErrorTranslations.CONNECTION_OPEN_FAILED,
				ex,
			)
		}
	}

	/**
	 * Check the status code returned from the server.
	 * If the status code indicates an error, a ChipGatewayException will be thrown.
	 *
	 * @param statusCode The status code we received from the server
	 * @throws ConnectionError If the server returned a HTTP error code
	 */
	@Throws(ConnectionError::class, InvalidRedirectUrlException::class)
	private fun checkHTTPStatusCode(statusCode: Int) {
		if (statusCode != 200) {
			throw ConnectionError(
				token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
				ErrorTranslations.INVALID_HTTP_STATUS,
				statusCode,
			)
		}
	}

	private fun checkProcessCancelled() {
		if (Thread.currentThread().isInterrupted) {
			throw performProcessCancelled()
		}
	}

	private fun performProcessCancelled(): ThreadTerminateException {
		val resp =
			TerminateType().apply {
				sessionIdentifier = sessionId
				result = ChipGatewayStatusCodes.STOPPED
			}
		return performProcessCancelled(getResource(terminateUrl), resp)
	}

	private fun performProcessCancelled(
		resource: String,
		msg: ResponseType?,
	): ThreadTerminateException {
		LOG.debug { "Sending terminate message due to process cancellation." }
		this.isInterrupted = true
		val t =
			Thread(
				{
					try {
						sendMessage<_root_ide_package_.org.openecard.ws.chipgateway.CommandType>(
							resource,
							mapper.writeValueAsString(msg),
							CommandType::class.java,
						)
					} catch (ex: JsonProcessingException) {
						LOG.debug(ex) { "Error sending terminating message." }
					} catch (ex: ConnectionError) {
						LOG.debug(ex) { "Error sending terminating message." }
					} catch (ex: InvalidRedirectUrlException) {
						LOG.debug(ex) { "Error sending terminating message." }
					} catch (ex: ChipGatewayDataError) {
						LOG.debug(ex) { "Error sending terminating message." }
					} finally {
						// close connection as nobody will send a message after termination
						try {
							conn?.close()
						} catch (ex: IOException) {
							LOG.error(ex) { "Failed to close connection to server." }
						}
					}
				},
				"ChipGateway-terminate",
			)
		t.start()

		return ThreadTerminateException("ChipGateway protocol interrupted.")
	}

	@Throws(
		ConnectionError::class,
		InvalidRedirectUrlException::class,
		ChipGatewayDataError::class,
		ThreadTerminateException::class,
		JsonProcessingException::class,
	)
	private fun sendMessageInterruptableAndCheckTermination(
		resource: String,
		resp: ResponseType,
	): CommandType {
		// stop messages are sent in the background
		if (ChipGatewayStatusCodes.STOPPED == resp.result) {
			throw performProcessCancelled(resource, resp)
		}
		// all other messages are sent normally and if an interrupt is hit, send terminate in background thread
		try {
			val msg = mapper.writeValueAsString(resp)
			return sendMessageInterruptable<CommandType>(
				resource,
				msg,
				org.openecard.ws.chipgateway.CommandType::class.java,
			)
		} catch (ex: ThreadTerminateException) {
			LOG.info(ex) { "Sending message ${resp.javaClass.simpleName} interrupted. Shutting down." }
			throw performProcessCancelled()
		}
	}

	@Throws(
		ConnectionError::class,
		InvalidRedirectUrlException::class,
		ChipGatewayDataError::class,
		ThreadTerminateException::class,
	)
	private fun <T> sendMessageInterruptable(
		resource: String,
		msg: String,
		resClass: Class<T>,
	): T {
		val task =
			FutureTask { sendMessage(resource, msg, resClass) }
		Thread(task, "HTTP-Client-" + HTTP_THREAD_NUM.getAndIncrement()).start()

		try {
			return task.get()
		} catch (ex: ExecutionException) {
			val cause = ex.cause
			when (cause) {
				is ConnectionError,
				is InvalidRedirectUrlException,
				is ChipGatewayDataError,
				is RuntimeException,
				-> {
					throw cause
				}
				else -> {
					throw RuntimeException("Unexpected exception raised by HTTP message sending thread.", cause)
				}
			}
		} catch (ex: InterruptedException) {
			LOG.debug { "Sending HTTP message interrupted." }
			task.cancel(true)

			// force new connection because this one may be unfinished and thus unusable
			try {
				conn?.shutdown()
			} catch (ignore: IOException) {
			}

			throw ThreadTerminateException("Interrupt received while sending HTTP message.")
		}
	}

	@Throws(ConnectionError::class, InvalidRedirectUrlException::class, ChipGatewayDataError::class)
	private fun <T> sendMessage(
		resource: String,
		msg: String,
		resClass: Class<T>,
	): T = sendMessage<T>(resource, msg, resClass, true)

	@Throws(ConnectionError::class, InvalidRedirectUrlException::class, ChipGatewayDataError::class)
	private fun <T> sendMessage(
		resource: String,
		msg: String,
		resClass: Class<T>,
		tryAgain: Boolean,
	): T {
		try {
			// open initial connection
			if (conn == null || canReuse || (!conn!!.isOpen() && canReuse)) {
				openHttpStream()
			}

			// prepare request

			val reqMsg = StringEntity(msg, ContentType.create("application/json", "UTF-8"))

			val req =
				BasicHttpEntityEnclosingRequest("POST", resource).apply {
					setHeader("Accept", "application/json")
					entity = reqMsg
					setHeader(reqMsg.contentType)
					setHeader("Content-Length", reqMsg.contentLength.toString())
				}

			if (LOG_HTTP_MESSAGES) {
				dumpHttpRequest(LOG, "before adding content", req)
			}
			setDefaultHeader(req, tlsHandler.getServerAddress())
			if (LOG_HTTP_MESSAGES) {
				LOG.debug { msg }
			}

			// send request and receive response
			LOG.debug { "Sending HTTP request." }
			val response = httpExecutor.execute(req, conn, httpCtx)
			canReuse = reuseStrategy.keepAlive(response, httpCtx)
			LOG.debug { "HTTP response received." }
			val statusCode = response.statusLine.statusCode
			checkHTTPStatusCode(statusCode)

			conn?.receiveResponseEntity(response)
			val entity = response.entity
			val entityData = toByteArray(entity.content)
			if (LOG_HTTP_MESSAGES) {
				dumpHttpResponse(LOG, response, entityData)
			}

			// convert entity and return it
			val resultObj = parseResultObj(entityData, resClass)
			return resultObj
		} catch (ex: IOException) {
			if (!Thread.currentThread().isInterrupted && tryAgain) {
				val errorMsg = "ChipGateway server closed the connection. Trying to connect again."
				LOG.debug(ex) { errorMsg }
				LOG.info { errorMsg }
				canReuse = false
				return sendMessage<T>(resource, msg, resClass, false)
			} else {
				throw ConnectionError(
					token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
					ErrorTranslations.CONNECTION_OPEN_FAILED,
					ex,
				)
			}
		} catch (ex: HttpException) {
			throw ConnectionError(
				token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
				ErrorTranslations.HTTP_ERROR,
				ex,
			)
		}
	}

	@Throws(ChipGatewayDataError::class, InvalidRedirectUrlException::class)
	private fun <T> parseResultObj(
		msg: ByteArray?,
		msgClass: Class<T>,
	): T {
		try {
			val obj = mapper.readValue<T>(msg, msgClass)
			return obj
		} catch (ex: IOException) {
			val errorMsg = "Failed to convert response to JSON data type."
			LOG.warn { errorMsg }
			throw ChipGatewayDataError(
				token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
				ErrorTranslations.INVALID_CHIPGATEWAY_MSG,
				ex,
			)
		}
	}

	private fun getResource(uri: URI): String {
		var path = uri.path
		val query = uri.query
		// correct and combine path
		path = path ?: "/"
		val resource = if (query == null) path else "$path?$query"
		return resource
	}

	@Throws(
		VersionTooOld::class,
		ChipGatewayDataError::class,
		ConnectionError::class,
		InvalidRedirectUrlException::class,
		AuthServerException::class,
	)
	fun sendHello(): TerminateType {
		try {
			val challenge = generateRandom(32)
			helloReq =
				HelloRequestType().apply {
					sessionIdentifier = sessionId
					version = "$major.$minor.$patch"
					setChallenge(challenge)
				}

			// send Hello
			val helloReqMsg = mapper.writeValueAsString(helloReq)
			val helloResp =
				sendMessageInterruptable<HelloResponseType>(
					getResource(helloUrl),
					helloReqMsg,
					org.openecard.ws.chipgateway.HelloResponseType::class.java,
				)
			processHelloResponse(helloResp)

			// send GetCommand
			val cmdReq = createGetCommandRequest()
			val cmdReqMsg = mapper.writeValueAsString(cmdReq)
			var cmdResp: CommandType
			try {
				cmdResp =
					sendMessageInterruptable<CommandType>(
						getResource(getCommandUrl),
						cmdReqMsg,
						org.openecard.ws.chipgateway.CommandType::class.java,
					)
			} catch (ex: ThreadTerminateException) {
				performProcessCancelled()
				throw ex
			}

			// send messages to the server as long as there is no termination response
			while (cmdResp.terminate == null) {
				val tokensReq = cmdResp.listTokensRequest
				val certReq = cmdResp.listCertificatesRequest
				val signReq = cmdResp.signRequest

				cmdResp =
					if (tokensReq != null) {
						processTokensRequest(tokensReq)
					} else if (certReq != null) {
						processCertificatesRequest(certReq)
					} else if (signReq != null) {
						processSignRequest(signReq)
					} else {
						throw ChipGatewayDataError(
							token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
							ErrorTranslations.INVALID_CHIPGATEWAY_MSG,
						)
					}
			}

			// return the last message (terminate type)
			return cmdResp.terminate
		} catch (ex: JsonProcessingException) {
			throw ChipGatewayDataError(
				token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR),
				ErrorTranslations.INVALID_CHIPGATEWAY_MSG,
				ex,
			)
		} finally {
			// clear token cache and delete all pins in it
			tokenCache.clearPins()

			// display GUI if needed
			showDialogThread?.start()

			try {
				// in case we are interrupted, terminate is sent in the background, so don't close just yet
				conn?.let {
					if (!isInterrupted) {
						it.close()
					}
				}
			} catch (ex: IOException) {
				LOG.error(ex) { "Failed to close connection to server." }
			}

			// disconnect all slots which have been connected in the process
			for (nextSlot in connectedSlots) {
				LOG.debug { "Disconnecting card with slotHandle=${ByteUtils.toHexString(nextSlot)}." }

				val req = CardApplicationDisconnect()
				// req.setAction(ActionType.RESET);
				req.connectionHandle =
					HandlerBuilder
						.create()
						.setSlotHandle(nextSlot)
						.buildConnectionHandle()

				dispatcher.safeDeliver(req)
			}
		}
	}

	@Throws(
		AuthServerException::class,
		InvalidRedirectUrlException::class,
		VersionTooOld::class,
		ChipGatewayDataError::class,
	)
	private fun processHelloResponse(helloResp: HelloResponseType) {
		// check if we have been interrupted
		checkProcessCancelled()

		val rCode = helloResp.result

		// check for codes which don't break the process immediately
		if (ChipGatewayStatusCodes.OK == rCode ||
			ChipGatewayStatusCodes.UPDATE_RECOMMENDED == rCode ||
			ChipGatewayStatusCodes.UPDATE_REQUIRED == rCode
		) {
			// validate hello response (e.i. challenge and server signature)
			if (ChipGatewayProperties.isValidateChallengeResponse) {
				LOG.debug { "Validating challenge-response signature." }
				validateSignature(helloResp)
			} else {
				LOG.warn { "Skipping the validation of the challenge-response signature." }
			}

			// check version and propose to update the client
			val dlUrl = helloResp.downloadAddress
			val mustUpdate = ChipGatewayStatusCodes.UPDATE_REQUIRED == rCode
			dlUrl?.let {
				// create dialog thread which will be executed when the protocol is finished
				createUpdateDialog(it, mustUpdate)
			}
			if (mustUpdate) {
				// stop protocol
				throw VersionTooOld(
					token.finalizeErrorAddress(ResultMinor.CLIENT_ERROR),
					ErrorTranslations.VERSION_OUTDATED,
				)
			}

			// TODO: check WebOrigin
		} else {
			LOG.error { "Received an error form the ChipGateway server {$rCode}." }
			throw ChipGatewayDataError(rCode, "Received an error form the ChipGateway server.")
			// 	    switch (rCode) {
// 		// TODO: evaluate result
// 	    }
		}
	}

	private fun createGetCommandRequest(): GetCommandType {
		val cmd =
			GetCommandType().apply {
				sessionIdentifier = sessionId
			}

		// add token info
		try {
			val helper = ListTokens(mutableListOf(), addonCtx, sessionId)
			val matchedTokens = helper.findTokens()
			cmd.getTokenInfo().addAll(matchedTokens)
		} catch (ex: UnsupportedAlgorithmException) {
			throw RuntimeException("Unexpected error in empty token filter.", ex)
		} catch (ex: WSHelper.WSException) {
			LOG.error { "Error requesting initial list of tokens." }
		}

		return cmd
	}

	@Throws(
		ConnectionError::class,
		JsonProcessingException::class,
		InvalidRedirectUrlException::class,
		ChipGatewayDataError::class,
	)
	private fun processTokensRequest(tokensReq: ListTokensRequestType): CommandType {
		// check if we have been interrupted
		checkProcessCancelled()

		var tokensResp = ListTokensResponseType().apply { sessionIdentifier = sessionId }

		try {
			tokensResp = waitForTokens(tokensReq)
		} catch (ex: UnsupportedAlgorithmException) {
			LOG.error(ex) { "Unsupported algorithm used." }
			tokensResp.result = ChipGatewayStatusCodes.INCORRECT_PARAMETER
		} catch (ex: WSHelper.WSException) {
			LOG.error(ex) { "Unknown error." }
			tokensResp.result = ChipGatewayStatusCodes.OTHER
		} catch (ex: ThreadTerminateException) {
			LOG.info(ex) { "Chipgateway process interrupted." }
			tokensResp.result = ChipGatewayStatusCodes.STOPPED
		} catch (ex: InterruptedException) {
			LOG.info(ex) { "Chipgateway process interrupted." }
			tokensResp.result = ChipGatewayStatusCodes.STOPPED
		} catch (ex: TimeoutException) {
			LOG.info(ex) { "Waiting for new tokens timed out." }
			tokensResp.result = ChipGatewayStatusCodes.TIMEOUT
		}

		return sendMessageInterruptableAndCheckTermination(getResource(listTokensUrl), tokensResp)
	}

	@Throws(
		UnsupportedAlgorithmException::class,
		WSHelper.WSException::class,
		InterruptedException::class,
		TimeoutException::class,
	)
	private fun waitForTokens(tokensReq: ListTokensRequestType): ListTokensResponseType {
		val waitSecondsBig = tokensReq.maxWaitSeconds
		val waitMillis = getWaitMillis(waitSecondsBig)

		val startTime = Date()

		val helper = ListTokens(tokensReq.tokenInfo, addonCtx, sessionId)
		do {
			// build list of matching tokens
			val matchedTokens = helper.findTokens()

			// save handles of connected cards
			connectedSlots.addAll(helper.getConnectedSlots())

			// return if tokens have been found or no specific set of tokens has been requested
			if (matchedTokens.isNotEmpty() || tokensReq.tokenInfo.isEmpty()) {
				return ListTokensResponseType().apply {
					sessionIdentifier = sessionId
					result = ChipGatewayStatusCodes.OK
					tokenInfo.addAll(matchedTokens)
				}
			}

			// TODO: use real wait mechanism on the SAL implementation
			Thread.sleep(1000)
		} while ((Date().time - startTime.time) < waitMillis)

		throw TimeoutException("Waiting for ListTokens timed out.")
	}

	@Throws(
		ConnectionError::class,
		JsonProcessingException::class,
		InvalidRedirectUrlException::class,
		ChipGatewayDataError::class,
	)
	private fun processCertificatesRequest(certReq: ListCertificatesRequestType): CommandType {
		// check if we have been interrupted
		checkProcessCancelled()

		val waitMillis = getWaitMillis(certReq.maxWaitSeconds)

		// run the actual stuff in the background, so we can wait and terminate if needed
		val action =
			FutureTask<ListCertificatesResponseType>(
				object : Callable<ListCertificatesResponseType?> {
					@Throws(Exception::class)
					override fun call(): ListCertificatesResponseType {
						val certResp =
							ListCertificatesResponseType().apply {
								setSessionIdentifier(sessionId)
							}

						var pin: CharArray? = null
						try {
							pin = getPin(certReq.pin)
							val slotHandle = certReq.slotHandle
							val certInfos =
								ListCertificates(
									tokenCache,
									certReq.certificateFilter,
									pin,
									sessionId,
									slotHandle,
								).certificates

							certResp.getCertificateInfo().addAll(certInfos)
							certResp.setResult(ChipGatewayStatusCodes.OK)
							return certResp
						} finally {
							pin?.let {
								Arrays.fill(it, ' ')
							}
						}
					}
				},
			)
		val t =
			Thread(action, "CertificatesRequest-Task-" + TASK_THREAD_NUM.getAndIncrement())
				.apply {
					setDaemon(true)
				}

		t.start()

		var certResp =
			ListCertificatesResponseType().apply {
				sessionIdentifier = sessionId
			}

		try {
			// wait for thread to finish
			certResp = action.get(waitMillis, TimeUnit.MILLISECONDS)
		} catch (ex: TimeoutException) {
			LOG.info(ex) { "Background task took longer than the timeout value permitted." }
			action.cancel(true) // cancel task
			// wait for task to finish, so the SC stack can not get confused
			try {
				t.join()
				certResp.result = ChipGatewayStatusCodes.TIMEOUT
			} catch (ignore: InterruptedException) {
				// send stop message
				certResp.result = ChipGatewayStatusCodes.STOPPED
			}
		} catch (ex: ExecutionException) {
			LOG.error(ex) { "Background task produced an exception." }
			val cause = ex.cause
			when (cause) {
				is RemotePinException -> {
					LOG.error(ex) { "Error getting encrypted PIN." }
					certResp.result = ChipGatewayStatusCodes.INCORRECT_PARAMETER
				}

				is ParameterInvalid -> {
					LOG.error(ex) { "Error while processing the certificate filter parameters." }
					certResp.result = ChipGatewayStatusCodes.INCORRECT_PARAMETER
				}

				is SlotHandleInvalid -> {
					LOG.error(cause) { "No token for the given slot handle found." }
					certResp.result = ChipGatewayStatusCodes.UNKNOWN_SLOT
				}

				is NoSuchDid -> {
					LOG.error(cause) { "DID does not exist." }
					certResp.result = ChipGatewayStatusCodes.UNKNOWN_DID
				}

				is SecurityConditionUnsatisfiable -> {
					LOG.error(cause) { "DID can not be authenticated." }
					certResp.result = ChipGatewayStatusCodes.SECURITY_NOT_SATISFIED
				}

				is CertificateException -> {
					LOG.error(cause) { "Certificate could not be processed." }
					certResp.result = ChipGatewayStatusCodes.OTHER
				}

				is WSHelper.WSException -> {
					LOG.error(cause) { "Unknown error." }
					certResp.result = ChipGatewayStatusCodes.OTHER
				}

				is ThreadTerminateException -> {
					LOG.error(cause) { "Chipgateway process interrupted." }
					certResp.result = ChipGatewayStatusCodes.STOPPED
				}

				else -> {
					LOG.error(cause) { "Unknown error during list certificate operation." }
					certResp.result = ChipGatewayStatusCodes.OTHER
				}
			}
		} catch (ex: InterruptedException) {
			val msg = "Interrupted while waiting for background task."
			LOG.debug(ex) { msg }
			LOG.info { msg }
			action.cancel(true) // cancel task
			// send stop message
			certResp.result = ChipGatewayStatusCodes.STOPPED
		}

		return sendMessageInterruptableAndCheckTermination(getResource(listCertsUrl), certResp)
	}

	@Throws(
		ConnectionError::class,
		JsonProcessingException::class,
		InvalidRedirectUrlException::class,
		ChipGatewayDataError::class,
	)
	private fun processSignRequest(signReq: SignRequestType): CommandType {
		// check if we have been interrupted
		checkProcessCancelled()

		val waitMillis = getWaitMillis(signReq.maxWaitSeconds)

		// run the actual stuff in the background, so we can wait and terminate if needed
		val action =
			FutureTask<SignResponseType>(
				object : Callable<SignResponseType?> {
					@Throws(Exception::class)
					override fun call(): SignResponseType {
						val signResp = SignResponseType().apply { sessionIdentifier = sessionId }

						val slotHandle = signReq.slotHandle
						val didName = signReq.didName

						var pin: CharArray? = null
						try {
							pin = getPin(signReq.pin)
							val signer = Signer(tokenCache, slotHandle, didName, pin)
							val signature = signer.sign(signReq.message)

							signResp.signature = signature
							signResp.result = ChipGatewayStatusCodes.OK

							return signResp
						} finally {
							pin?.let {
								Arrays.fill(it, ' ')
							}
						}
					}
				},
			)
		val t =
			Thread(action, "SignRequest-Task-" + TASK_THREAD_NUM.getAndIncrement()).apply {
				isDaemon = true
			}
		t.start()

		var signResp = SignResponseType().apply { sessionIdentifier = sessionId }

		try {
			// wait for thread to finish
			signResp = action.get(waitMillis, TimeUnit.MILLISECONDS)
		} catch (ex: TimeoutException) {
			LOG.info(ex) { "Background task took longer than the timeout value permitted." }
			action.cancel(true) // cancel task
			// wait for task to finish, so the SC stack can not get confused
			try {
				t.join()
				signResp.result = ChipGatewayStatusCodes.TIMEOUT
			} catch (ignore: InterruptedException) {
				// send stop message
				signResp.result = ChipGatewayStatusCodes.STOPPED
			}
		} catch (ex: ExecutionException) {
			LOG.error(ex) { "Background task produced an exception." }
			val cause = ex.cause
			when (cause) {
				is RemotePinException -> {
					LOG.error(cause) { "Error getting encrypted PIN." }
					signResp.result = ChipGatewayStatusCodes.INCORRECT_PARAMETER
				}

				is ParameterInvalid -> {
					LOG.error(cause) { "Error while processing the certificate filter parameters." }
					signResp.result = ChipGatewayStatusCodes.INCORRECT_PARAMETER
				}

				is SlotHandleInvalid -> {
					LOG.error(cause) { "No token for the given slot handle found." }
					signResp.result = ChipGatewayStatusCodes.UNKNOWN_SLOT
				}

				is NoSuchDid -> {
					LOG.error(cause) { "DID does not exist." }
					signResp.result = ChipGatewayStatusCodes.UNKNOWN_DID
				}

				is PinBlocked -> {
					LOG.error(cause) { "PIN is blocked." }
					signResp.result = ChipGatewayStatusCodes.PIN_BLOCKED
				}

				is SecurityConditionUnsatisfiable -> {
					LOG.error(cause) { "DID can not be authenticated." }
					signResp.result = ChipGatewayStatusCodes.SECURITY_NOT_SATISFIED
				}

				is WSHelper.WSException -> {
					LOG.error(cause) { "Unknown error." }
					signResp.result = ChipGatewayStatusCodes.OTHER
				}

				is ThreadTerminateException -> {
					LOG.error(cause) { "Chipgateway process interrupted." }
					signResp.result = ChipGatewayStatusCodes.STOPPED
				}

				else -> {
					LOG.error(cause) { "Unknown error during sign operation." }
					signResp.result = ChipGatewayStatusCodes.OTHER
				}
			}
		} catch (ex: InterruptedException) {
			val msg = "Interrupted while waiting for background task."
			LOG.debug(ex) { msg }
			LOG.info { msg }
			action.cancel(true) // cancel task
			// send stop message
			signResp.result = ChipGatewayStatusCodes.STOPPED
		}

		return sendMessageInterruptableAndCheckTermination(getResource(signUrl), signResp)
	}

	@Throws(AuthServerException::class, InvalidRedirectUrlException::class)
	private fun validateSignature(helloResp: HelloResponseType) {
		try {
			val challenge = helloReq!!.challenge
			var signature = helloResp.signature
			signature = signature ?: ByteArray(0) // prevent null value

			SignatureVerifier(challenge)
				.validate(signature)
		} catch (ex: IOException) {
			val msg = "Failed to load ChipGateway truststore from bundled truststore file."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: KeyStoreException) {
			val msg = "ChipGateway truststore is inoperable."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: NoSuchAlgorithmException) {
			val msg = "Invalid algorithm used during signature verification."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: CertificateException) {
			val msg = "Invalid certificate used in signature."
			LOG.error(ex) { msg }
			throw RuntimeException(msg, ex)
		} catch (ex: SignatureInvalid) {
			throw AuthServerException(
				token.finalizeErrorAddress(ResultMinor.COMMUNICATION_ERROR),
				ErrorTranslations.SIGNATURE_INVALID,
				ex,
			)
		}
	}

	private fun isUpdateNecessary(minimumVersion: String): Boolean {
		val requiredVersion = SemanticVersion(minimumVersion)
		val appVersion = version
		return appVersion.isOlder(requiredVersion)
	}

	private fun showErrorMessage(msg: String) {
		val title: String? = LANG.translationForKey("error.dialog.title")
		val subMsg: String? = LANG.translationForKey("error.dialog.submessage")
		val fullMsg = String.format("%s%n%n%s", msg, subMsg)
		gui.obtainMessageDialog().showMessageDialog(fullMsg, title, DialogType.ERROR_MESSAGE)
	}

	@Throws(ChipGatewayDataError::class, InvalidRedirectUrlException::class)
	private fun createUpdateDialog(
		dlUrl: String?,
		updateRequired: Boolean,
	) {
		// stop here when hide dialog system property is set and the update is optional
		if (!updateRequired && ChipGatewayProperties.isHideUpdateDialog) {
			return
		}

		// check that dlUrl conforms to the spec

		// only show if we have a download URL
		if (dlUrl?.isNotEmpty() == true) {
			try {
				val uri = URI(dlUrl)
				if (!"https".equals(uri.scheme, ignoreCase = true)) {
					showErrorMessage(LANG.translationForKey("error.server_wrong_config"))
					throw MalformedURLException("Download URL is not an https URL.")
				}
				val dlHost = uri.host
				if (ChipGatewayProperties.isUseUpdateDomainWhitelist &&
					!AllowedUpdateDomains.instance().isAllowedDomain(dlHost)
				) {
					val msg = String.format("Update host name (%s) does not match allowed domain names.", dlHost)
					LOG.error { msg }

					showErrorMessage(LANG.translationForKey("error.server_wrong_config"))
					throw MalformedURLException(String.format("Download URL host (%s) is not in whitelist.", dlHost))
				}

				val dialog = UpdateDialog(gui, dlUrl, updateRequired)
				showDialogThread =
					Thread(
						{ dialog.display() },
						"Update-Dialog-" + TASK_THREAD_NUM.getAndIncrement(),
					).apply { isDaemon = true }
			} catch (ex: MalformedURLException) {
				val msg = "Received malformed download URL from server."
				LOG.error(ex) { msg }
				throw ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR), msg, ex)
			} catch (ex: URISyntaxException) {
				val msg = "Received malformed download URL from server."
				LOG.error(ex) { msg }
				throw ChipGatewayDataError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR), msg, ex)
			}
		}
	}

	private fun getWaitMillis(waitSecondsBig: BigInteger?): Long {
		// limit timeout to WAIT_MAX_MILLIS
		return if (waitSecondsBig == null || waitSecondsBig >= BigInteger.valueOf(WAIT_MAX_MILLIS)) {
			WAIT_MAX_MILLIS
		} else {
			waitSecondsBig.toLong() * 1000
		}
	}

	@Throws(RemotePinException::class)
	private fun getPin(encryptedPin: String?): CharArray? {
		if (ChipGatewayProperties.isRemotePinAllowed && encryptedPin != null) {
			if (pinKey != null) {
				try {
					// decrypt PIN
					val jwe = JsonWebEncryption()

					// specify algorithmic constraints
					val algConstraints =
						AlgorithmConstraints(
							AlgorithmConstraints.ConstraintType.WHITELIST,
							KeyManagementAlgorithmIdentifiers.DIRECT,
						)
					jwe.setAlgorithmConstraints(algConstraints)
					val encConstraints =
						AlgorithmConstraints(
							AlgorithmConstraints.ConstraintType.WHITELIST,
							ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
							ContentEncryptionAlgorithmIdentifiers.AES_192_CBC_HMAC_SHA_384,
							ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512,
							ContentEncryptionAlgorithmIdentifiers.AES_128_GCM,
							ContentEncryptionAlgorithmIdentifiers.AES_192_GCM,
							ContentEncryptionAlgorithmIdentifiers.AES_256_GCM,
						)
					jwe.setContentEncryptionAlgorithmConstraints(encConstraints)

					// perform decryption
					jwe.setCompactSerialization(encryptedPin)
					jwe.setKey(pinKey.getKey())
					val pinBytes = jwe.getPlaintextBytes()

					// check if PIN is a sane value
					val pin: CharArray?
					if (pinBytes == null || pinBytes.size == 0) {
						val msg = "No or empty PIN received from ChipGateway server, despite a key being present."
						LOG.warn { msg }
						pin = null
					} else {
						val charBuf = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pinBytes))
						pin = CharArray(charBuf.remaining())
						charBuf.get(pin)
						if (charBuf.hasArray()) {
							Arrays.fill(charBuf.array(), ' ')
						}
					}
					return pin
				} catch (ex: JoseException) {
					throw RemotePinException("Error decrypting PIN.", ex)
				}
			} else {
				// PIN sent but no key provided, raise error for the server
				throw RemotePinException("Encrypted PIN received, but no key for decryption is available.")
			}
		} else {
			// no pin sent, let user supply the pin
			return null
		}
	}

	companion object {
		private const val WAIT_MAX_MILLIS =
			(
				60 * 60 * 1000 // 60 min
			).toLong()
		private val LANG: I18n = I18n.getTranslation("chipgateway")
		private val TASK_THREAD_NUM = AtomicInteger(1)
		private val HTTP_THREAD_NUM = AtomicInteger(1)
		private const val LOG_HTTP_MESSAGES = true
	}
}
