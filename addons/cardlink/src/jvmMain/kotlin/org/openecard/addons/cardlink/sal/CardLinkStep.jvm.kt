/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.addons.cardlink.sal

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import kotlinx.serialization.encodeToString
import org.openecard.addon.Context
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.addons.cardlink.sal.gui.CardLinkUserConsent
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.crypto.common.sal.did.DidInfos
import org.openecard.gui.ResultStatus
import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.executor.ExecutionEngine
import java.util.*

private val logger = KotlinLogging.logger {}

class CardLinkStep(val aCtx: Context) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	val gui = aCtx.userConsent

	override fun getFunctionType(): FunctionType {
		return FunctionType.DIDAuthenticate
	}

	override fun perform(req: DIDAuthenticate, internalData: MutableMap<String, Any>): DIDAuthenticateResponse {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val ws = getWsPair(dynCtx)
		val uc = CardLinkUserConsent(ws, aCtx, req.connectionHandle)

		val navigator: UserConsentNavigator = gui.obtainNavigator(uc)
		val exec = ExecutionEngine(navigator)
		try {
			val guiResult = exec.process()
			when (guiResult) {
				ResultStatus.OK -> {
					//continue
				}
				else -> {
					// fail
					return DIDAuthenticateResponse().apply {
						// TODO: obtain proper error from gui
						result = WSHelper.makeResultUnknownError("CardLink GUI process failed.")
					}
				}
			}
		} catch (ex: ThreadTerminateException) {
			// fail
			return DIDAuthenticateResponse().apply {
				result = WSHelper.makeResultError(ECardConstants.Minor.SAL.CANCELLATION_BY_USER, "CardLink failed GUI process has been interrupted.")
			}
		}

		val cardSessionId = dynCtx.get(CardLinkKeys.WS_SESSION_ID) as String
		val conHandle = dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType

		val egkData = readEgkData(conHandle, cardSessionId)
		sendEgkData(egkData, cardSessionId, ws)

		waitForCardLinkFinish()

		return DIDAuthenticateResponse().apply {
			result = WSHelper.makeResultOK()
		}
	}

	private fun readEgkData(conHandle: ConnectionHandleType, cardSessionId: String): RegisterEgk {
		val infos = DidInfos(aCtx.dispatcher, null, conHandle)
		val gdoDs = infos.getDataSetInfo("EF.GDO").read()
		val versionDs = infos.getDataSetInfo("EF.Version2").read()
		val cvcEgkAuthEc = infos.getDataSetInfo("EF.C.eGK.AUT_CVC.E256").read()
		val cvcEgkCaEc = infos.getDataSetInfo("EF.C.CA.CS.E256").read()
		val atrDs = infos.getDataSetInfo("EF.ATR").read()
		val x509EsignAuthEc = infos.getDataSetInfo("EF.C.CH.AUT.E256").read()
		val x509EsignAuthRsa = infos.getDataSetInfo("EF.C.CH.AUT.R2048").read()

		return RegisterEgk(
			cardSessionId = cardSessionId,
			gdo = gdoDs,
			cardVersion = versionDs,
			cvcAuth = cvcEgkAuthEc,
			cvcCA = cvcEgkCaEc,
			atr = atrDs,
			x509AuthECC = x509EsignAuthEc,
			x509AuthRSA = x509EsignAuthRsa
		)
	}

	private fun sendEgkData(regEgk: RegisterEgk, cardSessionId: String, ws: WsPair) {
		val correlationId = UUID.randomUUID().toString()
		val egkEnvelope = GematikEnvelope(
			regEgk,
			correlationId,
			cardSessionId,
		)
		val egkEnvelopeMsg = cardLinkJsonFormatter.encodeToString(egkEnvelope)
		ws.socket.send(egkEnvelopeMsg)
	}

	private fun waitForCardLinkFinish() {
		TODO("Not yet implemented")
	}

}
