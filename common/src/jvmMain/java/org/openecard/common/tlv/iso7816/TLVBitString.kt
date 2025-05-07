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
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag.Companion.BITSTRING_TAG
import java.util.Arrays

/**
 *
 * @author Tobias Wich
 */
open class TLVBitString
	@JvmOverloads
	constructor(
		tlv: TLV,
		tagNumWithClass: Long = BITSTRING_TAG.tagNumWithClass,
	) {
		private val tlv: TLV
		private var data: ByteArray?

		init {
			if (tlv.tagNumWithClass != tagNumWithClass) {
				throw TLVException("Type numbers don't match.")
			}

			this.tlv = tlv
			this.data = tlv.value
			this.data = Arrays.copyOfRange(data, 1, data!!.size)
		}

		fun isSet(pos: Int): Boolean {
			val i = pos / 8
			val j = 1 shl 7 - pos % 8

			if ((this.data != null) && (data!!.size > i)) {
				val k = data!![i].toInt()
				return (k and j) != 0
			} else {
				return false
			}
		}
	}
