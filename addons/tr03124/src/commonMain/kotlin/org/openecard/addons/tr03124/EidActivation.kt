package org.openecard.addons.tr03124

import org.openecard.addons.tr03124.eac.UiStep
import org.openecard.addons.tr03124.eac.UiStepImpl
import org.openecard.addons.tr03124.transport.EserviceCertTracker
import org.openecard.addons.tr03124.transport.EserviceClientImpl
import org.openecard.addons.tr03124.transport.newKtorClientBuilder
import org.openecard.addons.tr03124.xml.ConnectionHandleType
import org.openecard.addons.tr03124.xml.ECardConstants
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.utils.common.generateSessionId
import org.openecard.utils.serialization.toPrintable
import kotlin.random.Random

object EidActivation {
	/**
	 * Starts an EAC process by giving the TCToken location and a SAL session to operate in.
	 * The terminal name is optional as it can be set later when performing PACE.
	 * Prior to returning the [UiStep], the EAC data are read from the server.
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	@Throws(BindingException::class)
	suspend fun startEacProcess(
		clientInfo: ClientInformation,
		tokenUrl: String,
		session: SmartcardSalSession,
		terminalName: String?,
		random: Random = Random.Default,
		startPaos: StartPaos =
			StartPaos(
				requestId = random.generateSessionId(),
				profile = ECardConstants.Profile.ECARD_1_1,
				sessionIdentifier = session.sessionId,
				connectionHandle =
					ConnectionHandleType(
						contextHandle = random.nextBytes(32).toUByteArray().toPrintable(),
						slotHandle = random.nextBytes(32).toUByteArray().toPrintable(),
					),
				userAgent = clientInfo.userAgent.toXmlType(),
				supportedAPIVersions = clientInfo.apiVersion.map { it.toXmlType() },
				supportedDIDProtocols = clientInfo.supportedDidProtocols,
			),
	): UiStep {
		val certTracker = EserviceCertTracker()
		val clientFactory = newKtorClientBuilder(certTracker)
		val eserviceClient = EserviceClientImpl(certTracker, clientFactory)

		val token = eserviceClient.fetchToken(tokenUrl)
		val eidServer = eserviceClient.buildEidServerInterface(startPaos)
		val eac1Input = eidServer.start()
		val uiStep: UiStep = UiStepImpl.createStep(session, terminalName, token, eserviceClient, eidServer, eac1Input)
		return uiStep
	}
}
