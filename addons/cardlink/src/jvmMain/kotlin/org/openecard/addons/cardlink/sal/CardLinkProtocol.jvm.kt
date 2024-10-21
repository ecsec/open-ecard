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
import org.openecard.addons.cardlink.ws.WsPair
import org.openecard.common.DynamicContext

const val CARDLINK_PROTOCOL_ID = "https://gematik.de/protocols/cardlink"


fun setWsPair(dynCtx: DynamicContext, ws: WsPair) {
	dynCtx.put(CardLinkKeys.WEBSOCKET, ws)
}

fun getWsPair(dynCtx: DynamicContext): WsPair {
	return dynCtx.getPromise(CardLinkKeys.WEBSOCKET).deref() as WsPair
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
	const val WS_SESSION_ID = "${prefix}WS_SESSION_ID"
	const val CARD_SESSION_ID = "${prefix}CARD_SESSION_ID"
	const val PHONE_NUMBER_REGISTERED = "${prefix}PHONE_NUMBER_REGISTERED"
	const val LAST_SENT_MESSAGE_ID = "${prefix}LAST_SENT_MESSAGE_ID"
	const val WEBSOCKET = "${prefix}WEBSOCKET"
	const val CORRELATION_ID_TAN_PROCESS = "${prefix}CORRELATION_ID_TAN"
	const val ICCSN = "${prefix}ICCSN"
	const val CLIENT_ERROR_CODE = "${prefix}CLIENT_ERROR_CODE"
	const val SERVICE_ERROR_CODE = "${prefix}SERVICE_ERROR_CODE"
	const val ERROR_MESSAGE = "${prefix}ERROR_MESSAGE"
	const val ICCSN_REASSIGNMENT_TIMESTAMP = "${prefix}ICCSN_REASSIGNMENT_TIMESTAMP"
}
