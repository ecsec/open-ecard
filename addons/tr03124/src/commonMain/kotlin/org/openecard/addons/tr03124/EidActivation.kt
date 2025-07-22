package org.openecard.addons.tr03124

import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.addons.tr03124.eac.UiStepImpl
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EidServerPaos
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.Eac1Input
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.sal.iface.SalSession
import org.openecard.utils.common.generateSessionId
import kotlin.random.Random

object EidActivation {
	/**
	 * Starts an EAC process by giving the TCToken location and a SAL session to operate in.
	 * The terminal name is optional as it can be set later when performing PACE.
	 * Prior to returning the [UiStep], the EAC data are read from the server.
	 */
	@Throws(BindingException::class)
	suspend fun startEacProcess(
		clientInfo: ClientInformation,
		tokenUrl: String,
		session: SalSession,
		terminalName: String?,
		random: Random = Random.Default,
		startPaos: StartPaos =
			StartPaos(requestId = random.generateSessionId(), ECardConstants.Profile.ECARD_1_1, session.sessionId),
	): UiStep {
		val eserviceClient: EserviceClient = TODO()
		val token: TcToken = eserviceClient.fetchToken(tokenUrl)
		val eidServer: EidServerInterface = EidServerPaos(token, startPaos, random)
		val eac1Input =
			when (val msg = eidServer.start()) {
				is DidAuthenticateRequest<*> if msg.data is Eac1Input -> msg as DidAuthenticateRequest<Eac1Input>
				else -> throw InvalidServerData(eserviceClient, "Invalid DIDAuthenticate message received from eID-Server")
			}
		val uiStep: UiStep = UiStepImpl.createStep(session, terminalName, token, eserviceClient, eidServer, eac1Input)
		return uiStep
	}
}
