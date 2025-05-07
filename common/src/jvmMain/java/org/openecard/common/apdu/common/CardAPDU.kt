/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
package org.openecard.common.apdu.common

@Suppress("ktlint:standard:property-naming")

/**
 * Base for all APDU types.
 *
 * @author Moritz Horsch
 */
open class CardAPDU {
	// TODO: make field inaccessible

	protected var protectedData = ByteArray(0)

	open var data: ByteArray
		get() {
			if (protectedData.isEmpty()) {
				return ByteArray(0)
			} else {
				val ret = ByteArray(protectedData.size)
				System.arraycopy(protectedData, 0, ret, 0, protectedData.size)

				return ret
			}
		}
		set(value) {
			protectedData = value
		}

// 	fun getData(): ByteArray {
// 		if (data.isEmpty()) {
// 			return ByteArray(0)
// 		} else {
// 			val ret = ByteArray(data.size)
// 			System.arraycopy(data, 0, ret, 0, data.size)
//
// 			return ret
// 		}
// 	}
//
// 	open fun setData(data: ByteArray) {
// 		this.data = data
// 	}

	/**
	 * Returns the data field of the APDU.
	 *
	 * @return Data field. May be empty if no data is available

	 * Sets the data field of the APDU.
	 *
	 * @param data Data field
	 */

	companion object {
		// TODO: what is this comment "do not use with bitmask

		const val x00: Byte = 0x00.toByte()

		/**
		 * 0xFF byte. Do not use me with a bit mask!
		 */
		const val xFF: Byte = 0xFF.toByte()
	}
}
