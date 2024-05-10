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

import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.addon.bind.*
import org.openecard.mobile.activation.Websocket
import org.openecard.mobile.activation.common.CommonCardLinkControllerFactory.WS_KEY

class ActivateAction : AppPluginAction {
    private var aCtx: Context? = null

    override fun execute(
		body: RequestBody,
		parameters: MutableMap<String, String>,
		headers: Headers,
		attachments: MutableList<Attachment>,
		extraParams: MutableMap<String, Any>
    ): BindingResult {
        val ws: Websocket = extraParams[WS_KEY] as Websocket?
            ?: return BindingResult(BindingResultCode.WRONG_PARAMETER)
                .setResultMessage("Missing websocket in dynamic context.")

        // call CardLink process
        val proc: CardLinkProcess = CardLinkProcess(ws)
        return proc.start()
    }

    @kotlin.Throws(ActionInitializationException::class)
	override fun init(aCtx: Context) {
        this.aCtx = aCtx
    }

    override fun destroy(force: Boolean) {
        this.aCtx = null
    }

}
