/****************************************************************************
 * Copyright (C) 2018-2020 ecsec GmbH.
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
package org.openecard.common.ifd

import org.openecard.common.util.StringUtils

/**
 * Eac PIN Status calls.
 * See BSI TR-03130-3, B.14.2
 *
 * @author Tobias Wich
 */
enum class PacePinStatus(
	vararg codes: ByteArray,
) {
	UNKNOWN,

	/**
	 * PIN activated, 3 tries left.
	 */
	RC3(
		StringUtils.toByteArray("9000"),
		StringUtils.toByteArray("63C3"),
	),

	/**
	 * PIN activated, 2 tries left.
	 */
	RC2(
		StringUtils.toByteArray("63C2"),
	),

	/**
	 * PIN suspended, 1 try left, CAN needs to be entered.
	 */
	RC1(
		StringUtils.toByteArray("63C1"),
		StringUtils.toByteArray("6985"),
	),

	/**
	 * PIN blocked, 0 tries left.
	 */
	BLOCKED(
		// blocked, deactivated or suspended, we hope the best and pretend it is blocked, so the user has a chance to fix it
		StringUtils.toByteArray("63C0"),
		StringUtils.toByteArray("6982"),
		StringUtils.toByteArray("6983"),
	),

	/**
	 * PIN/Card not activated.
	 */
	DEACTIVATED(
		StringUtils.toByteArray("6984"),
	),
	;

	private val codes: List<ByteArray> = codes.toList()

	companion object {
		fun getCodes(): List<ByteArray> = entries.flatMap { it.codes }

		/**
		 * Gets the PIN status for the given response code.
		 *
		 * @param code PIN status code as returned by the PIN status APDU.
		 * @return The PIN status matching the given code.
		 * @throws IllegalArgumentException Thrown in case the given code is not a specified status code.
		 */
		@JvmStatic
		@Throws(IllegalArgumentException::class)
		fun fromCode(code: ByteArray): PacePinStatus {
			for (ps in entries) {
				for (refCode in ps.codes) {
					if (refCode.contentEquals(code)) {
						return ps
					}
				}
			}
			// no status found
			return PacePinStatus.UNKNOWN
		}
	}
}
