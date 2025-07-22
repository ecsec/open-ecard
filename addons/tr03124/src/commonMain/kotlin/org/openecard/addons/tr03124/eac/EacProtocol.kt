package org.openecard.addons.tr03124.eac

import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.TcToken
import org.openecard.addons.tr03124.Tr03124Binding
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.sal.iface.SalSession
import org.openecard.sal.iface.dids.PaceDid

interface UiStep {
	/**
	 * Gets the EAC data needed for the UI.
	 */
	val guiData: EacUiData

	/**
	 * Cancels the process and returns a response for returning to the eService.
	 */
	fun cancel(): BindingResponse

	/**
	 * Gets the PACE DID, that needs to be used for the secure channel establishment.
	 * CHAT and CertificateDescription need to be provided based on [EacUiData].
	 * The card is connected in the process if it isn't connected yet.
	 */
	fun getPaceDid(terminalName: String?): PaceDid

	/**
	 * Disconnects the card.
	 * This may be used if the user is expected to remove the card after a failed PACE attempt, which is typical in
	 * mobile cases.
	 */
	fun disconnectCard()

	/**
	 * Process EAC terminal and chip authentication.
	 */
	@Throws(BindingException::class)
	suspend fun processAuthentication(): EidServerStep
}

interface EidServerStep {
	/**
	 * Cancels the process and returns a response for returning to the eService.
	 */
	fun cancel(): BindingResponse

	/**
	 * Process commands received by the eID-Server until there are no more commands.
	 */
	@Throws(BindingException::class)
	suspend fun processEidServerLogic(): BindingResponse
}
