/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
package org.openecard.sal.protocol.pincompare

import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.addon.sal.SALProtocolBaseImpl

/**
 * Implements the PIN Compare protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.
 *
 * @author Dirk Petrautzki
 */
class PINCompareProtocol : SALProtocolBaseImpl() {
	@Throws(ActionInitializationException::class)
	override fun init(ctx: Context) {
		addStatelessStep(DIDUpdateStep(ctx.dispatcher))
		addStatelessStep(DIDAuthenticateStep(ctx.dispatcher))
	}

	// nothing to see here ... move along
	override fun destroy(force: Boolean) = Unit
}
