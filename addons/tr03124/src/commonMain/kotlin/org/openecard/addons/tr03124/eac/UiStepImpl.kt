package org.openecard.addons.tr03124.eac

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.TcToken
import org.openecard.addons.tr03124.transport.EidServerInterface
import org.openecard.addons.tr03124.transport.EserviceClient
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.Eac1Input
import org.openecard.sal.iface.SalSession
import org.openecard.sal.iface.dids.PaceDid

internal class UiStepImpl(
	private val ctx: UiStepCtx,
) : UiStep {
	class UiStepCtx(
		val session: SalSession,
		val token: TcToken,
		val eserviceClient: EserviceClient,
		val eidServer: EidServerInterface,
		val eac1Input: DidAuthenticateRequest<Eac1Input>,
		var terminalName: String?,
		val uiData: EacUiData,
	)

	override val guiData: EacUiData = ctx.uiData

	override fun cancel(): BindingResponse {
		TODO("Not yet implemented")
	}

	override fun getPaceDid(terminalName: String?): PaceDid {
		TODO("Not yet implemented")
	}

	override fun disconnectCard() {
		TODO("Not yet implemented")
	}

	override suspend fun processAuthentication(): EidServerStep {
		TODO("Not yet implemented")
	}

	companion object {
		internal fun createStep(
			session: SalSession,
			terminalName: String?,
			token: TcToken,
			eserviceClient: EserviceClient,
			eidServer: EidServerInterface,
			eac1Input: DidAuthenticateRequest<Eac1Input>,
		): UiStep {
			val uiData: EacUiData = TODO()
			val ctx = UiStepCtx(session, token, eserviceClient, eidServer, eac1Input, terminalName, uiData)
			return UiStepImpl(ctx)
		}
	}
}
