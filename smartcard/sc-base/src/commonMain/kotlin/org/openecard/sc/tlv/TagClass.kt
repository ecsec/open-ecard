/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

package org.openecard.sc.tlv

/**
 *
 * @author Tobias Wich
 */
enum class TagClass(
	val num: UByte,
) {
	UNIVERSAL(0u),
	APPLICATION(1u),
	CONTEXT(2u),
	PRIVATE(3u),
	;

	companion object {
		@OptIn(ExperimentalStdlibApi::class)
		fun getTagClass(octet: UByte): TagClass {
			val classByte = ((octet.toInt() shr 6) and 0x03)
			return when (classByte) {
				0 -> UNIVERSAL
				1 -> APPLICATION
				2 -> CONTEXT
				3 -> PRIVATE
				else -> {
					throw IllegalStateException("Logic error when processing tag class byte: ${octet.toHexString()}")
				}
			}
		}
	}
}
