/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

/**
 *
 * @author Tobias Wich
 */
object ChipGatewayStatusCodes {
	private const val PREFIX = "http://ws.openecard.org/result/"
	private const val PREFIX_ERR = "http://ws.openecard.org/result/error/"
	private const val PREFIX_WARN = "http://ws.openecard.org/result/warning/"

	const val OK: String = PREFIX + "ok"

	const val UNKNOWN_SESSION: String = PREFIX_ERR + "unknownSessionIdentifier"
	const val UNSUITABLE_SESSION: String = PREFIX_ERR + "unsuitableSessionIdentifier"
	const val UNSUITABLE_CHALLENGE: String = PREFIX_ERR + "unsuitableChallenge"
	const val INCORRECT_PARAMETER: String = PREFIX_ERR + "incorrectParameter"
	const val OTHER: String = PREFIX_ERR + "other"
	const val STOPPED: String = PREFIX_ERR + "stopped"
	const val UPDATE_REQUIRED: String = PREFIX_ERR + "updateRequired"
	const val UPDATE_RECOMMENDED: String = PREFIX_WARN + "updateRecommended"
	const val TIMEOUT: String = PREFIX_ERR + "timeout"
	const val UNKNOWN_SLOT: String = PREFIX_ERR + "unknownSlotHandle"
	const val SECURITY_NOT_SATISFIED: String = PREFIX_ERR + "securityConditionNotSatisfied"
	const val PIN_BLOCKED: String = PREFIX_ERR + "pinBlocked"
	const val UNKNOWN_DID: String = PREFIX_ERR + "unknownDIDName"

	fun isOk(code: String?): Boolean = OK == code || UPDATE_RECOMMENDED == code

	fun isError(code: String?): Boolean = !isOk(code)
}
