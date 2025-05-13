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

/**
 * GET CHALLENGE command
 * See ISO/IEC 7816-4 Section 7.5.3
 *
 * @author Moritz Horsch
 */
class GetChallenge : CardCommandAPDU {
	/**
	 * Creates a new GET CHALLENGE command.
	 * APDU: 0x00 0x84 0x00 0x00 0xLC
	 *
	 * @param length Expected length of the challenge

	 * Creates a new GET CHALLENGE command.
	 * APDU: 0x00 0x84 0x00 0x00 0x08
	 */
	@JvmOverloads
	constructor(length: Byte = 0x08.toByte()) : super(
		x00,
		GET_CHALLENGE_INS,
		x00,
		x00,
	) {
		setLE(length)
	}

	/**
	 * Creates a new GET CHALLENGE command.
	 * APDU: 0x00 0x84 0x00 0x00 0xLC
	 *
	 * @param length Expected length of the challenge
	 */
	constructor(length: Int) : super(
		x00,
		GET_CHALLENGE_INS,
		x00,
		x00,
	) {
		le = length
	}

	companion object {
		/**
		 * GET CHALLENGE command instruction byte
		 */
		private const val GET_CHALLENGE_INS = 0x84.toByte()
	}
}
