/****************************************************************************
 * Copyright (C) 2013-2025 ecsec GmbH.
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

/**
 * Wrapper for the wait for change request message.
 *
 * @author Johannes Schmölz
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class WaitForChangeRequest(
	val sessionIdentifier: String,
)

/**
 * Check the request parameters and wrap them in a `org.openecard.addons.status.WaitForChangeRequest` class.
 *
 * @param parameters The request parameters.
 * @return A org.openecard.addons.status.WaitForChangeRequest wrapping the parameters.
 * @throws StatusException Thrown in case not all required parameters are present.
 */
@Throws(StatusException::class)
fun buildWaitForChangeRequest(parameters: Map<String, String>?) =
	parameters?.get("session")?.let {
		WaitForChangeRequest(it)
	} ?: run {
		throw StatusException("Mandatory parameter session is missing.")
	}
