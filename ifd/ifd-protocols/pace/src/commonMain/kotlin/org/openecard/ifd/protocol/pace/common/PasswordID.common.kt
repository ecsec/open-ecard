/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace.common

/**
 * Implements the different password identifier.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.1.
 *
 * @author Moritz Horsch
 */
enum class PasswordID(
	/**
	 * Returns the byte representation of the password ID.
	 *
	 * @return Byte representation
	 */
	val byte: Byte,
) {
	MRZ(0x01.toByte()),
	CAN(0x02.toByte()),
	PIN(0x03.toByte()),
	PUK(0x04.toByte()),
	;

	val byteAsString: String
		/**
		 * Returns the byte representation of the password ID as a string.
		 *
		 * @return Byte representation as a string
		 */
		get() = byte.toString()

	companion object {
		/**
		 * Parses a string to the password ID.
		 *
		 * @param type Type
		 * @return PasswordID
		 */
		@JvmStatic
		fun parse(type: String): PasswordID? =
			if (type.matches("[1-4]".toRegex())) {
				parse(type.toInt())
			} else {
				valueOf(type)
			}

		/**
		 * Parses a byte to the password ID.
		 *
		 * @param type Type
		 * @return PasswordID
		 */
		@JvmStatic
		fun parse(type: Number): PasswordID? =
			when (type.toInt()) {
				0x01 -> MRZ
				0x02 -> CAN
				0x03 -> PIN
				0x04 -> PUK
				else -> null
			}
	}
}
