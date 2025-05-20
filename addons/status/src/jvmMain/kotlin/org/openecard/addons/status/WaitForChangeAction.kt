/****************************************************************************
 * Copyright (C) 2013-2016 HS Coburg.
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

package org.openecard.addons.status

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.Context
import org.openecard.addon.EventHandler
import org.openecard.addon.bind.AppPluginAction
import org.openecard.addon.bind.Attachment
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addon.bind.Headers
import org.openecard.addon.bind.RequestBody

/**
 * Action processing WaitForChange messages.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */

private val logger = KotlinLogging.logger { }

class WaitForChangeAction : AppPluginAction {
	private var eventHandler: EventHandler? = null

	override fun init(ctx: Context) {
		eventHandler = ctx.eventHandler
	}

	override fun destroy(force: Boolean) {
		eventHandler = null
	}

	override fun execute(
		body: RequestBody?,
		parameters: Map<String, String>?,
		headers: Headers?,
		attachments: List<Attachment>?,
		extraParams: Map<String, Any>?,
	): BindingResult =
		when (val hdl = eventHandler) {
			null -> {
				logger.error { "Error in WaitForChangeAction" }
				BindingResult(BindingResultCode.INTERNAL_ERROR)
			}

			else -> {
				try {
					val statusRequest = buildWaitForChangeRequest(parameters)
					val status = hdl.next(statusRequest.sessionIdentifier)
					StatusResponseBodyFactory().createWaitForChangeResponse(status)
				} catch (e: StatusException) {
					BindingResult(BindingResultCode.WRONG_PARAMETER, e.message)
				} catch (e: Exception) {
					logger.error(e) { "Error in WaitForChangeAction" }
					BindingResult(BindingResultCode.INTERNAL_ERROR)
				}
			}
		}
}
