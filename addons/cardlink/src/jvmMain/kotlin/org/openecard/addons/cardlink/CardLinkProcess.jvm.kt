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
 */

package org.openecard.addons.cardlink

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.CreateSession
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.common.WSHelper
import org.openecard.mobile.activation.Websocket

class CardLinkProcess constructor(private val ctx: Context, private val ws: Websocket) {

	private val dispatcher = ctx.dispatcher

    fun start(): BindingResult {
		val conHandle = openSession()
		performDidAuth(conHandle)

		val phoneNumber = requestPhoneNumber()
		sendPhoneNumber(phoneNumber)
		sendOtp()

		val requiredCardTypes = setOf("http://ws.gematik.de/egk/1.0.0")
		val cardHandle = waitForCard(requiredCardTypes)
		authCard(cardHandle)
		val cardData = readPatientData(cardHandle)
		sendPatientData(cardData)
		handleRemoteApdus(cardHandle)

		// no error means success
        return BindingResult(BindingResultCode.OK)
    }

	private fun openSession(): ConnectionHandleType {
		// Perform a CreateSession to initialize the SAL.
		val createSession = CreateSession()
		val createSessionResp = dispatcher.safeDeliver(createSession) as CreateSessionResponse

		// Check CreateSessionResponse
		WSHelper.checkResult(createSessionResp)

		// Update ConnectionHandle.
		val connectionHandle = createSessionResp.connectionHandle

		return connectionHandle
	}

	private fun performDidAuth(conHandle: ConnectionHandleType) {
		TODO("Not yet implemented")
	}

	private fun waitForCard(requiredCardTypes: Set<String>): Any {
		TODO("Not yet implemented")
	}

	private fun authCard(cardHandle: Any) {
		TODO("Not yet implemented")
	}

	private fun readPatientData(cardHandle: Any): Any {
		TODO("Not yet implemented")
	}

	private fun requestPhoneNumber(): String {
		TODO("Not yet implemented")
	}

	private fun sendPhoneNumber(phoneNumber: String) {
		TODO("Not yet implemented")
	}

	private fun sendOtp() {
		// ask user a few times until code is accepted by the server
		TODO("Not yet implemented")
	}

	private fun sendPatientData(cardData: Any) {
		TODO("Not yet implemented")
	}

	private fun handleRemoteApdus(cardHandle: Any) {
		TODO("Not yet implemented")
	}

}
