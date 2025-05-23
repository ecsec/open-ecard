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

package org.openecard.addons.cardlink.sal.gui

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.addon.Context
import org.openecard.addons.cardlink.ws.WsPair
import org.openecard.common.util.SysUtils
import org.openecard.gui.definition.UserConsentDescription

private const val TITLE = "CardLink User Consent"
const val CONSENT_TYPE = "CardLink"

class CardLinkUserConsent(
	ws: WsPair,
	addonCtx: Context,
	isPhoneRegistered: Boolean,
	sessionHandle: ConnectionHandleType,
) : UserConsentDescription(TITLE, CONSENT_TYPE) {
	init {
		steps.apply {
			if (!isPhoneRegistered) {
				add(PhoneStep(ws))
				add(TanStep(ws))
			}
			// on mobile we definitely have NFC so we will need a CAN
			if (SysUtils.isMobileDevice) {
				add(EnterCanStep(ws, addonCtx, sessionHandle))
				// desktop might have a contact based reader allowing to connect without can
				// if direct connect fails, EnterCanStep will be added by DirectConnectStep
			} else {
				add(DirectConnectStep(ws, addonCtx, sessionHandle))
			}
		}
	}
}
