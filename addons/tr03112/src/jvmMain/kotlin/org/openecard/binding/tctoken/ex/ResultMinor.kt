/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
package org.openecard.binding.tctoken.ex

/**
 * The class contains constants for the result minor according to BSI-TR-03124-1 v1.2 section 2.5.5.2.
 *
 * @author Hans-Martin Haase
 */
object ResultMinor {
	/**
	 * Indicates that the eID-Client failed to set up a trusted channel to the eID-Server.
	 */
	const val TRUSTED_CHANNEL_ESTABLISHMENT_FAILED: String = "trustedChannelEstablishmentFailed"

	/**
	 * Indicates that the user aborted the authentication.
	 * This includes also the abortion due to entering a wrong PIN or if no card is available.
	 */
	const val CANCELLATION_BY_USER: String = "cancellationByUser"

	/**
	 * Indicates that the eID-Server encountered an error.
	 * The exact error is communicated to the eService directly by the eID-Server.
	 */
	const val SERVER_ERROR: String = "serverError"

	/**
	 * Indicates that an error occurred which is not covered by the other error codes.
	 */
	const val CLIENT_ERROR: String = "clientError"

	/**
	 * Indicates that no refresh URL could be determined.
	 */
	const val COMMUNICATION_ERROR: String = "communicationError"
}
