/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import org.openecard.addon.Context
import org.openecard.addons.cg.activate.TlsConnectionHandler
import org.openecard.addons.cg.ex.AuthServerException
import org.openecard.addons.cg.ex.ChipGatewayDataError
import org.openecard.addons.cg.ex.ChipGatewayUnknownError
import org.openecard.addons.cg.ex.ConnectionError
import org.openecard.addons.cg.ex.ErrorTranslations
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.ex.ResultMinor
import org.openecard.addons.cg.ex.VersionTooOld
import org.openecard.addons.cg.tctoken.TCToken
import org.openecard.ws.chipgateway.TerminateType
import java.util.concurrent.Callable

/**
 *
 * @author Tobias Wich
 */
class ChipGatewayTask(
	private val token: TCToken,
	private val ctx: Context,
) : Callable<TerminateType?> {
	@Throws(
		ConnectionError::class,
		VersionTooOld::class,
		InvalidTCTokenElement::class,
		ChipGatewayDataError::class,
		InvalidRedirectUrlException::class,
		AuthServerException::class,
		ChipGatewayUnknownError::class,
	)
	override fun call(): TerminateType {
		val tlsHandler = TlsConnectionHandler(token).apply { setUpClient() }

		val cg = ChipGateway(tlsHandler, token, ctx)
		val result = cg.sendHello()

		if (ChipGatewayStatusCodes.isError(result.result)) {
			throw ChipGatewayUnknownError(
				token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
				ErrorTranslations.SERVER_SENT_ERROR,
				result.result,
			)
		}

		return result
	}
}
