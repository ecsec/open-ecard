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

package org.openecard.addons.cardlink

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addons.cardlink.sal.CARDLINK_PROTOCOL_ID
import org.openecard.addons.cardlink.sal.setProcessWebsocket
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.WSHelper
import org.openecard.common.util.HandlerUtils
import org.openecard.mobile.activation.Websocket

class CardLinkProcess constructor(private val ctx: Context, private val ws: Websocket) {

	private val dispatcher = ctx.dispatcher

    fun start(): BindingResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val conHandle = openSession()
		dynCtx.put(TR03112Keys.SESSION_CON_HANDLE, HandlerUtils.copyHandle(conHandle))
		setProcessWebsocket(dynCtx, ws)
		val cardHandle = performDidAuth(conHandle)
		handleRemoteApdus(cardHandle)
		destroySession(cardHandle)

		// no error means success
        return BindingResult(BindingResultCode.OK)
    }

	@Throws(WSHelper.WSException::class)
	private fun openSession(): ConnectionHandleType {
		// Perform a CreateSession to initialize the SAL
		val createSession = CreateSession()
		val createSessionResp = dispatcher.safeDeliver(createSession) as CreateSessionResponse

		// Check CreateSessionResponse
		WSHelper.checkResult(createSessionResp)

		// Update ConnectionHandle.
		val connectionHandle = createSessionResp.connectionHandle

		return connectionHandle
	}

	@Throws(WSHelper.WSException::class)
	private fun destroySession(conHandle: ConnectionHandleType) {
		// Perform a CloseSession to close the SAL
		val closeSession = DestroySession().apply {
			connectionHandle = conHandle
		}
		val closeSessionResp = dispatcher.safeDeliver(closeSession) as DestroySessionResponse

		// Check CloseSessionResponse
		WSHelper.checkResult(closeSessionResp)
	}

	@Throws(WSHelper.WSException::class)
	private fun performDidAuth(conHandle: ConnectionHandleType): ConnectionHandleType {
		// Perform a DIDAuthenticate to authenticate the user
		val didAuth = DIDAuthenticate().apply {
			connectionHandle = conHandle
			authenticationProtocolData = DIDAuthenticationDataType().apply {
				protocol = CARDLINK_PROTOCOL_ID
			}
		}
		val didAuthResp = dispatcher.safeDeliver(didAuth) as DIDAuthenticateResponse

		// Check DIDAuthenticateResponse
		WSHelper.checkResult(didAuthResp)

		//return didAuthResp.authenticationProtocolData
		TODO("Not yet implemented")
	}

	private fun handleRemoteApdus(cardHandle: Any) {
		TODO("Not yet implemented")
	}

}
