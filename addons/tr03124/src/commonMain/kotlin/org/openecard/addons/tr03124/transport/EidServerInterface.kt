package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.xml.AuthenticationProtocolData
import org.openecard.addons.tr03124.xml.AuthenticationRequestProtocolData
import org.openecard.addons.tr03124.xml.AuthenticationResponseProtocolData
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.Eac1Input
import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.ResponseType
import org.openecard.addons.tr03124.xml.TransmitRequest
import org.openecard.addons.tr03124.xml.TransmitResponse

interface EidServerInterface {
	@Throws(BindingException::class)
	suspend fun start(): DidAuthenticateRequest

	/**
	 * Send DID Authenticate response and handle response from server.
	 *
	 * If the answer from the server contains
	 * - a new DID Authenticate request, it's protocol data is returned.
	 * - an error or [org.openecard.addons.tr03124.xml.StartPaosResponse], the process is aborted with a binding exception.
	 * - any other non error response, then it is saved for being returned in [getFirstDataRequest] and `null` is returned.
	 */
	@Throws(BindingException::class)
	suspend fun sendDidAuthResponse(protocolData: AuthenticationResponseProtocolData): AuthenticationRequestProtocolData?

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
	@Throws(BindingException::class)
	suspend fun sendDataResponse(message: TransmitResponse): TransmitRequest?
}
