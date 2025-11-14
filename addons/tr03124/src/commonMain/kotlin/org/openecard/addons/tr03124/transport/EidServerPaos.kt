package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.xml.xml
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.NoTcToken
import org.openecard.addons.tr03124.TlsError
import org.openecard.addons.tr03124.UnknownClientError
import org.openecard.addons.tr03124.UnknownTrustedChannelError
import org.openecard.addons.tr03124.UnkownCvcChainError
import org.openecard.addons.tr03124.UnkownServerError
import org.openecard.addons.tr03124.UserCanceled
import org.openecard.addons.tr03124.xml.AuthenticationRequestProtocolData
import org.openecard.addons.tr03124.xml.AuthenticationResponseProtocolData
import org.openecard.addons.tr03124.xml.Body
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.DidAuthenticateResponse
import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.EmptyResponseDataType
import org.openecard.addons.tr03124.xml.EndpointReference
import org.openecard.addons.tr03124.xml.Envelope
import org.openecard.addons.tr03124.xml.Header
import org.openecard.addons.tr03124.xml.Paos
import org.openecard.addons.tr03124.xml.Result
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TransmitRequest
import org.openecard.addons.tr03124.xml.TransmitResponse
import org.openecard.addons.tr03124.xml.eacXml
import org.openecard.addons.tr03124.xml.toBody
import org.openecard.utils.common.generateSessionId
import org.openecard.utils.common.throwIf
import kotlin.random.Random

private val log = KotlinLogging.logger { }

internal class EidServerPaos(
	val serviceClient: EserviceClient,
	val serverUrl: String,
	val httpClient: HttpClient,
	val startPaos: StartPaos,
	val random: Random = Random.Default,
) : EidServerInterface {
	private val serviceString: String by lazy {
		val actions =
			listOf(
				// "http://www.bsi.bund.de/ecard/api/1.0#InitializeFramework",
				"urn:iso:std:iso-iec:24727:tech:schema:DIDAuthenticate",
				"urn:iso:std:iso-iec:24727:tech:schema:Transmit",
			)
		actions.joinToString("; ") { "\"$it\"" }
	}
	private val paosHeaderValue: String by lazy {
		"ver=\"${ECardConstants.PAOS_VERSION_20}\"; $serviceString"
	}

	private var phase: ProcessPhase = ProcessPhase.START

	private var curMsgId: String? = null
	private var remoteId: String? = null

	private fun setRemoteId(
		remoteId: String,
		relatesTo: String?,
	) {
		if (relatesTo != null && relatesTo != curMsgId) {
			throw InvalidServerData(serviceClient, "PAOS message ID mismatch")
		}
		this.remoteId = remoteId
	}

	private var firstTransmit: TransmitRequest? = null

	private var connectionTerminated: Boolean = false

	private suspend fun deliverMessage(soapEnv: Envelope): Envelope {
		log.info { "Delivering PAOS request message" }
		val resp =
			httpClient.post(serverUrl) {
				setBody(soapEnv)
				contentType(PaosContentType)
				headers {
					append("PAOS", paosHeaderValue)
				}
			}
		throwIf(!resp.status.isSuccess()) {
			InvalidServerData(serviceClient, "Server returned with status code ${resp.status.value}")
		}
		val respMsg: Envelope = resp.body()
		log.info { "Received PAOS response message" }

		// update terminated status
		if (respMsg.body.startPaosResponse != null) {
			connectionTerminated = true
		}

		// update remote message ID
		val msgId = respMsg.header?.messageID ?: throw InvalidServerData(serviceClient, "No message ID in PAOS response")
		val relatesTo = respMsg.header.relatesTo
		setRemoteId(msgId, relatesTo)

		return respMsg
	}

	private fun Body.wrapWithSoapEnv(): Envelope {
		val paos =
			Paos(
				mustUnderstand = true,
				actor = ECardConstants.ACTOR_NEXT,
				version = listOf(ECardConstants.PAOS_VERSION_20),
				endpointReference =
					listOf(
						EndpointReference(address = ECardConstants.PAOS_ADDRESS),
					),
			)
		curMsgId = random.generateSessionId()
		val header = Header(paos = paos, messageID = curMsgId, relatesTo = remoteId)
		val env = Envelope(header, this)
		return env
	}

	override suspend fun start(): DidAuthenticateRequest {
		log.info { "Sending StartPAOS message" }
		val req = startPaos.toBody().wrapWithSoapEnv()
		val res = deliverMessage(req)

		res.body.didAuthenticateRequest?.let {
			phase = ProcessPhase.AUTH
			return it
		}

		// server terminated the connection
		phase = ProcessPhase.DONE
		res.body.startPaosResponse?.let {
			throw UnkownServerError(serviceClient, "eID-Server terminated the connection")
		}

		// we received an unknown message from the server, there is no way to proceed
		throw InvalidServerData(
			serviceClient,
			"Server did not respond with DIDAuthenticateRequest",
		)
	}

	override suspend fun sendDidAuthResponse(
		protocolData: AuthenticationResponseProtocolData,
	): AuthenticationRequestProtocolData? {
		val msg = DidAuthenticateResponse(data = protocolData, result = Result.ok())
		val req = msg.toBody().wrapWithSoapEnv()
		val res = deliverMessage(req)

		// response is auth request
		res.body.didAuthenticateRequest?.let {
			return it.data
		}

		// response is transmit
		res.body.transmitRequest?.let {
			phase = ProcessPhase.TRANSMIT
			this.firstTransmit = it
			return null
		}

		// server terminated the connection
		phase = ProcessPhase.DONE
		res.body.startPaosResponse?.let {
			throw UnkownServerError(serviceClient, "eID-Server terminated the connection")
		}

		// we received an unknown message from the server, there is no way to proceed
		throw InvalidServerData(
			serviceClient,
			"Server did not respond with DIDAuthenticateRequest or Transmit",
		)
	}

	override suspend fun sendError(
		ex: BindingException,
		protocol: String,
	): Nothing {
		runCatching {
			log.debug { "Sending protocol error message to eID-Server" }

			val minor =
				when (ex) {
					is UnknownClientError -> ECardConstants.Minor.App.INT_ERROR
					is UnkownCvcChainError -> ECardConstants.Minor.SAL.EAC.DOC_VALID_FAILED
					is InvalidServerData -> ECardConstants.Minor.App.PARM_ERROR
					is UnkownServerError -> ECardConstants.Minor.Disp.COMM_ERROR
					is TlsError -> ECardConstants.Minor.Disp.CHANNEL_ESTABLISHMENT_FAILED
					is UnknownTrustedChannelError -> ECardConstants.Minor.Disp.CHANNEL_ESTABLISHMENT_FAILED
					is UserCanceled -> ECardConstants.Minor.SAL.CANCELLATION_BY_USER
					is NoTcToken -> ECardConstants.Minor.App.INCORRECT_PARM
				}
			val result =
				Result.error(
					minor,
					ex.message,
				)

			val req =
				when (phase) {
					ProcessPhase.AUTH -> {
						val protocolData = EmptyResponseDataType(protocol)
						val msg = DidAuthenticateResponse(data = protocolData, result = result)
						msg.toBody().wrapWithSoapEnv()
					}
					ProcessPhase.TRANSMIT -> {
						TransmitResponse(result, outputAPDU = listOf()).toBody().wrapWithSoapEnv()
					}
					else -> null
				}

			// update phase so we only send an error once
			phase = ProcessPhase.DONE

			// when we don't have a request, then we are not in a phase that needs to respond to the server
			req?.let {
				val res = deliverMessage(req)

				// error if we did't receive StartPaosResponse
				if (res.body.startPaosResponse == null) {
					// we received an unknown message from the server, there is no way to proceed
					throw InvalidServerData(
						serviceClient,
						"Server did not respond with StartPaosResponse",
					)
				} else {
					log.debug { "Received StartPaosResponse message" }
				}
			}
		}.onFailure { ex -> log.warn(ex) { "Failed to send error message to eID-Server" } }

		throw ex
	}

	override fun getFirstDataRequest(): TransmitRequest = checkNotNull(firstTransmit)

	override suspend fun sendDataResponse(message: TransmitResponse): TransmitRequest? {
		val req = message.toBody().wrapWithSoapEnv()
		val res = deliverMessage(req)

		// response is transmit
		res.body.transmitRequest?.let {
			return it
		}

		// server terminated the connection
		phase = ProcessPhase.DONE
		res.body.startPaosResponse?.let { pr ->
			if (pr.result.major == ECardConstants.Major.ERROR) {
				throw UnkownServerError(serviceClient, "eID-Server terminated the connection")
			} else {
				// normal termination
				return null
			}
		}

		// we received an unknown message from the server, there is no way to proceed
		throw InvalidServerData(
			serviceClient,
			"Server did not respond with DIDAuthenticateRequest or Transmit",
		)
	}

	private enum class ProcessPhase {
		START,
		AUTH,
		TRANSMIT,
		DONE,
	}

	companion object {
		private val PaosContentType = ContentType.parse("application/vnd.paos+xml")

		fun HttpClientConfig<*>.registerPaosNegotiation() {
			install(ContentNegotiation) {
				xml(
					format = eacXml,
					contentType = PaosContentType,
				)
				xml(
					format = eacXml,
					contentType = ContentType.Application.Soap.withParameter("q", "0.9"),
				)
				xml(
					format = eacXml,
					contentType = ContentType.Text.Xml.withParameter("q", "0.8"),
				)
				xml(
					format = eacXml,
					contentType = ContentType.Application.Xml.withParameter("q", "0.8"),
				)
			}
		}
	}
}
