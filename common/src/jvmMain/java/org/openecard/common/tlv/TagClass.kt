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
package org.openecard.common.tlv

/**
 *
 * @author Tobias Wich
 */
enum class TagClass(
	val num: Byte,
) {
	UNIVERSAL(0.toByte()),
	APPLICATION(1.toByte()),
	CONTEXT(2.toByte()),
	PRIVATE(3.toByte()),
	;

	companion object {
		@OptIn(ExperimentalStdlibApi::class)
		fun getTagClass(octet: Byte): TagClass {
			val classByte = ((octet.toInt() shr 6) and 0x03).toByte()
			return when (classByte) {
				0.toByte() -> UNIVERSAL
				1.toByte() -> APPLICATION
				2.toByte() -> CONTEXT
				3.toByte() -> PRIVATE
				else -> {
					throw IllegalArgumentException("what possible values are there in 2 bits?!? ${octet.toHexString()}")
				} // what possible values are there in 2 bits?!?
			}
		}
	}
}
