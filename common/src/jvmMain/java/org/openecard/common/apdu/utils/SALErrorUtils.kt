/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.common.apdu.utils

import org.openecard.common.ECardConstants
import org.openecard.common.ECardConstants.Minor
import org.openecard.common.util.ByteUtils

/**
 * Utility functions to determine SAL error codes based on APDU result codes of the tokens.
 *
 * @author Tobias Wich
 */
object SALErrorUtils {
	@JvmStatic
	fun getMajor(code: ByteArray): String {
		require(code.size == 2) { "Given response code is not exactly two bytes long." }
		val codeStr = ByteUtils.toHexString(code)

		return if ("9000" == codeStr || codeStr!!.startsWith("61")) {
			ECardConstants.Major.OK
		} else {
			ECardConstants.Major.ERROR
		}
	}
	@JvmStatic
	fun getMinor(code: ByteArray): String? {
		require(code.size == 2) { "Given response code is not exactly two bytes long." }
		val codeStr = ByteUtils.toHexString(code)
		val defaultError = Minor.App.UNKNOWN_ERROR

		// TODO: add more codes
		return if ("9000" == codeStr || codeStr!!.startsWith("61")) {
			null
		} else if (codeStr.startsWith("69")) {
			if (codeStr.endsWith("82")) {
				Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED
			} else {
				defaultError
			}
		} else {
			defaultError
		}
	}
}
