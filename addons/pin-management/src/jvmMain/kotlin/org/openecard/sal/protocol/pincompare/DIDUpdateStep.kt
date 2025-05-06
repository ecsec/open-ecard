/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.DIDUpdate
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher

/**
 * Implements the DIDUpdate step of the PIN Compare protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.1.3.
 *
 * @author Moritz Horsch
 * Creates a new DIDAuthenticateStep.
 *
 * @param dispatcher Dispatcher
 */
class DIDUpdateStep(
	private val dispatcher: Dispatcher?,
) : ProtocolStep<DIDUpdate?, DIDUpdateResponse?> {
	override fun getFunctionType(): FunctionType = FunctionType.DIDUpdate

	override fun perform(
		request: DIDUpdate?,
		internalData: Map<String?, Any?>?,
	): DIDUpdateResponse =
		WSHelper.makeResponse(
			DIDUpdateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)
}
