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

package org.openecard.addons.cardlink.sal

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import org.openecard.addon.Context
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep

class CardLinkStep(aCtx: Context) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	override fun getFunctionType(): FunctionType {
		return FunctionType.DIDAuthenticate
	}

	override fun perform(req: DIDAuthenticate, internalData: MutableMap<String, Any>): DIDAuthenticateResponse {
		TODO("Not yet implemented")
	}
}
