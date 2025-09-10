package org.openecard.addons.tr03124.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.UserCanceled
import org.openecard.addons.tr03124.runEacCatching
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.InputAPDUInfoType
import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.Result
import org.openecard.addons.tr03124.xml.TransmitRequest
import org.openecard.addons.tr03124.xml.TransmitResponse
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toCommandApdu
import org.openecard.utils.common.toUShort
import org.openecard.utils.serialization.toPrintable

private val log = KotlinLogging.logger { }

internal class EidServerStepImpl(
	val eserviceClient: EserviceClient,
	val eidServer: EidServerInterface,
	val con: SmartcardDeviceConnection,
) : EidServerStep {
	private var processingJob: Deferred<BindingResponse>? = null

	override suspend fun cancel(): BindingResponse =
		try {
			log.info { "EAC eID-Server Step cancelled" }
			runEacCatching(eserviceClient) {
				processingJob?.cancelAndJoin()

				processingJob = null
				UserCanceled(eserviceClient).toResponse()
			}
		} catch (ex: BindingException) {
			ex.toResponse()
		}

	override suspend fun processEidServerLogic(): BindingResponse =
		runEacCatching(eserviceClient) {
			log.info { "Processing eID-Server logic" }
			coroutineScope {
				val res =
					async {
						processEidServerLogicInt()
					}
				processingJob = res
				val resValue = res.await()
				processingJob = null
				resValue
			}
		}

	private suspend fun CoroutineScope.processEidServerLogicInt(): BindingResponse {
		var nextRequest: RequestType? = eidServer.getFirstDataRequest()
		while (nextRequest != null) {
			nextRequest =
				when (nextRequest) {
					is TransmitRequest -> {
						// check if we should cancel
						ensureActive()
						val nextResponse = processApdus(nextRequest)
						eidServer.sendDataResponse(nextResponse)
					}
					else -> {
						// wrong type
						return InvalidServerData(
							eserviceClient,
							"Server sent something other than a transmit request",
						).toResponse()
					}
				}
		}

		return eserviceClient.redirectToEservice()
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun processApdus(req: TransmitRequest): TransmitResponse {
		val result = mutableListOf<ResponseApdu>()

		for (next in req.inputAPDUInfo) {
			val apdu = next.inputAPDU.v.toCommandApdu()
			val resp = con.channel.transmit(apdu)
			result.add(resp)

			// check result
			if (!next.isValidResponse(resp)) {
				break
			}
		}

		// convert response
		return TransmitResponse(
			result =
				Result(
					ECardConstants.Major.OK,
					null,
					null,
				),
			requestId = req.requestId,
			outputAPDU =
				result.map {
					it.toBytes.toPrintable()
				},
		)
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun InputAPDUInfoType.isValidResponse(resp: ResponseApdu): Boolean =
	if (acceptableStatusCode.isNotEmpty()) {
		acceptableStatusCode.any { codeBytes ->
			when (codeBytes.v.size) {
				1 -> codeBytes.v[0] == resp.sw1
				2 -> resp.sw == codeBytes.v.toUShort(0)
				else -> throw IllegalArgumentException("Given acceptable status code is not 1 or 2 bytes long")
			}
		}
	} else {
		true
	}
