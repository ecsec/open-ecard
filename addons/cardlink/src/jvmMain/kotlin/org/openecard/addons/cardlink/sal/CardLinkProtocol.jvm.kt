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

import org.openecard.addon.Context
import org.openecard.addon.sal.SALProtocolBaseImpl
import org.openecard.mobile.activation.Websocket

const val CARDLINK_PROTOCOL_ID = "https://gematik.de/protocols/cardlink"


fun setProcessWebsocket(ws: Websocket) {
	TODO()
}

fun getProcessWebsocket(): Websocket {
	TODO()
}

class CardLinkProtocol : SALProtocolBaseImpl() {
	override fun init(aCtx: Context) {
		addOrderStep(CardLinkStep(aCtx))
	}

	override fun destroy(force: Boolean) {
	}
}

object CardLinkKeys {
	private const val prefix = "CardLink::"
	const val CON_HANDLE = "${prefix}CON_HANDLE"
}
