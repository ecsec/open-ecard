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
import org.openecard.addon.bind.AppPluginAction
import org.openecard.addon.bind.Attachment
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addon.bind.Headers
import org.openecard.addon.bind.RequestBody

private val logger = KotlinLogging.logger { }

/**
 * Action processing Status messages.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class StatusAction : AppPluginAction {
	private var statusHandler: StatusHandler? = null

	override fun init(ctx: Context) {
		statusHandler = StatusHandler(ctx)
	}

	override fun destroy(force: Boolean) {
		statusHandler = null
	}

	override fun execute(
		body: RequestBody?,
		parameters: Map<String, String>?,
		headers: Headers?,
		attachments: List<Attachment>?,
		extraParams: Map<String, Any>?,
	): BindingResult =
		when (val hdl = statusHandler) {
			null -> {
				logger.error { "Error in StatusAction addon: statusHandler not initialized" }
				BindingResult(BindingResultCode.INTERNAL_ERROR)
			}
			else -> {
				try {
					hdl.handleRequest(statusRequest(parameters))
				} catch (e: Exception) {
					logger.error(e) { "Error in StatusAction addon" }
					BindingResult(BindingResultCode.INTERNAL_ERROR)
				}
			}
		}
}
