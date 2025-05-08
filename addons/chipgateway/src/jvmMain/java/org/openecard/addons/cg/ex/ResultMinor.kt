/****************************************************************************
 * Copyright (C) 2015-2025 ecsec GmbH.
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
package org.openecard.addons.cg.ex

/**
 * The class contains constants for the result minor according to BSI-TR-03124-1 v1.2 section 2.5.5.2.
 *
 * @author Hans-Martin Haase
 */
enum class ResultMinor(
	val value: String,
) {
	/**
	 * Indicates that the user aborted the authentication.
	 * This includes also the abortion due to entering a wrong PIN or if no card is available.
	 */
	CANCELLATION_BY_USER("cancellationByUser"),

	/**
	 * Indicates that the eID-Server encountered an error.
	 * The exact error is communicated to the eService directly by the eID-Server.
	 */
	SERVER_ERROR("serverError"),

	/**
	 * Indicates that an error occurred which is not covered by the other error codes.
	 */
	CLIENT_ERROR("clientError"),

	/**
	 * Indicates that no refresh URL could be determined.
	 */
	COMMUNICATION_ERROR("communicationError"),
}
