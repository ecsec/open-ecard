package org.openecard.addons.tr03124.eac

import kotlinx.coroutines.CancellationException
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.sal.iface.DeviceUnsupported
import org.openecard.sal.iface.SalException
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse

interface UiStep {
	/**
	 * Gets the EAC data needed for the UI.
	 */
	val guiData: EacUiData

	/**
	 * Cancels the process and returns a response for returning to the eService.
	 */
	suspend fun cancel(): BindingResponse

	/**
	 * Gets the PACE DID, that needs to be used for the secure channel establishment.
	 * CHAT and CertificateDescription need to be provided based on [EacUiData].
	 * The card is connected in the process if it isn't connected yet.
	 */
	@Throws(DeviceUnsupported::class, SalException::class, IllegalStateException::class)
	fun getPaceDid(terminalName: String? = null): PaceDid

	/**
	 * Disconnects the card.
	 * This may be used if the user is expected to remove the card after a failed PACE attempt, which is typical in
	 * mobile cases.
	 */
	fun disconnectCard()

	/**
	 * Process EAC terminal and chip authentication.
	 */
	@Throws(BindingException::class, CancellationException::class)
	suspend fun processAuthentication(paceResponse: PaceEstablishChannelResponse): EidServerStep
}

interface EidServerStep {
	/**
	 * Cancels the process and returns a response for returning to the eService.
	 */
	suspend fun cancel(): BindingResponse

	/**
	 * Process commands received by the eID-Server until there are no more commands.
	 */
	@Throws(BindingException::class, CancellationException::class)
	suspend fun processEidServerLogic(): BindingResponse
}
