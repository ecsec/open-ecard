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
package org.openecard.sal.protocol.genericcryptography

import iso.std.iso_iec._24727.tech.schema.Encipher
import iso.std.iso_iec._24727.tech.schema.EncipherResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Dispatcher

/**
 * Implements the Encipher step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.5.
 *
 * @param dispatcher Dispatcher
 *
 * @author Moritz Horsch
 */
class EncipherStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<Encipher, EncipherResponse> {
	override fun getFunctionType(): FunctionType = FunctionType.Encipher

	// TODO Implement me
	override fun perform(
		request: Encipher,
		internalData: Map<String, Any>,
	): EncipherResponse =
		WSHelper.makeResponse<Class<EncipherResponse>, EncipherResponse>(
			iso.std.iso_iec._24727.tech.schema.EncipherResponse::class.java,
			org.openecard.common.WSHelper
				.makeResultUnknownError("Not supported yet."),
		)
}
