package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.xml.AuthenticationRequestProtocolData
import org.openecard.addons.tr03124.xml.AuthenticationResponseProtocolData
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.Result
import org.openecard.addons.tr03124.xml.TransmitRequest
import org.openecard.addons.tr03124.xml.TransmitResponse
import kotlin.coroutines.cancellation.CancellationException

interface EidServerInterface {
	@Throws(BindingException::class, CancellationException::class)
	suspend fun start(): DidAuthenticateRequest

	/**
	 * Mark the PAOS connection as validated, to allow further messages to be exchanged.
	 */
	fun setValidated()

	/**
	 * Send DID Authenticate response and handle response from server.
	 *
	 * If the answer from the server contains
	 * - a new DID Authenticate request, it's protocol data is returned.
	 * - an error or [org.openecard.addons.tr03124.xml.StartPaosResponse], the process is aborted with a binding exception.
	 * - any other non error response, then it is saved for being returned in [getFirstDataRequest] and `null` is returned.
	 */
	@Throws(BindingException::class, CancellationException::class)
	suspend fun sendDidAuthResponse(protocolData: AuthenticationResponseProtocolData): AuthenticationRequestProtocolData?

	/**
	 * Sends an error response message to the server.
	 * This method throws the provided BindingException, so the EAC process can be terminated with the error which
	 * caused the problem.
	 * The type of response message to send is determined by tracking the incoming request.
	 *
	 * @param ex The exception to throw after receiving the server response.
	 * @param protocol The protocol to set in the protocol data message.
	 */
	@Throws(BindingException::class)
	suspend fun sendError(
		ex: BindingException,
		protocol: String = ECardConstants.Protocol.EAC2,
	): Nothing

	/**
	 * Gets the first data request command after finishing the server authentication with [sendDidAuthResponse].
	 */
	@Throws(IllegalStateException::class)
	fun getFirstDataRequest(): TransmitRequest

	/**
	 * Exchange a data command.
	 * This method returns a new request, when the server returns a new command. In case of
	 * [org.openecard.addons.tr03124.xml.StartPaosResponse], `null` is returned.
	 */
	@Throws(BindingException::class, CancellationException::class)
	suspend fun sendDataResponse(message: TransmitResponse): TransmitRequest?
}
