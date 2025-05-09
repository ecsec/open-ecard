/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.util.ByteUtils

/**
 * RESET RETRY COUNTER command
 * See ISO/IEC 7816-4 Section 7.5.10
 *
 * @author Moritz Horsch
 */
class ResetRetryCounter : CardCommandAPDU {
	/**
	 * Creates a RESET RETRY COUNTER APDU.
	 * The APDU requires the password type and a new password. The old password is replaced by the given new one.
	 *
	 * @param password Password
	 * @param type Password type
	 */
	constructor(password: ByteArray?, type: Byte) : super(
		x00,
		RESET_RETRY_COUNTER_INS,
		0x02.toByte(),
		type,
	) {
		data = password!!
	}

	/**
	 * Creates a RESET RETRY COUNTER APDU.
	 * The APDU requires just the password type to reset the retry counter.
	 *
	 * @param type Password type
	 */
	constructor(type: Byte) : super(x00, RESET_RETRY_COUNTER_INS, 0x03.toByte(), type)

	/**
	 * Creates a RESET RETRY COUNTER APDU.
	 * The APDU requires a resetting code, new reference data and the password type. This means the old reference data
	 * is replaced with the new reference data after sending the APDU to the card.
	 *
	 * @param resettingCode The specific resetting code necessary to change the old reference data.
	 * @param newRefData The new reference data which shall replace the old reference data.
	 * @param type Password type
	 */
	constructor(resettingCode: ByteArray?, newRefData: ByteArray?, type: Byte) : super(
		x00,
		RESET_RETRY_COUNTER_INS,
		0x00.toByte(),
		type,
	) {
		data = ByteUtils.concatenate(resettingCode, newRefData)!!
	}

	val isWithNewPin: Boolean
		/**
		 * Checks whether just new reference data is necessary for the APDU.
		 *
		 * @return `TRUE` if P1 indicates that the data field contains just new reference data, otherwise `FALSE`.
		 */
		get() = p1 == 0x02.toByte()

	val isDataAbsent: Boolean
		/**
		 * Checks whether no data is necessary for the APDU.
		 *
		 * @return `TRUE` if P1 indicates that the data field have to be empty, otherwise `FALSE`.
		 */
		get() = p1 == 0x03.toByte()

	val isWithPukAndNewPin: Boolean
		/**
		 * Checks whether a resetting code (aka PUK) and new reference data (aka PIN) is necessary for the APDU.
		 *
		 * @return `TRUE` if P1 indicates that the data field have to contain a resetting code and without delimitation
		 * new reference data (a.k.a PIN) otherwise `FALSE`.
		 */
		get() = p1 == 0x00.toByte()

	val isWithPuk: Boolean
		/**
		 * Checks whether just a resetting code is required for the APDU.
		 *
		 * @return `TRUE` if P1 indicates that the data field have to contain just a resetting code, otherwise
		 * `FALSE`.
		 */
		get() = p1 == 0x01.toByte()

	companion object {
		/**
		 * RESET RETRY COUNTER command instruction byte
		 */
		private const val RESET_RETRY_COUNTER_INS = 0x2C.toByte()
	}
}
