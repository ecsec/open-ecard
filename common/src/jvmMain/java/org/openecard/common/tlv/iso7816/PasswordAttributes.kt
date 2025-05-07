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

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass
import org.openecard.common.util.ByteUtils.toInteger

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class PasswordAttributes(
	tlv: TLV,
) : TLVType(tlv) {
	// NOTE: initialize the optional parts, if it is not done the compiler will return a error
	var passwordFlags: TLVBitString? = null
		private set
	var passwordType: Int = 0 // enum PasswordType
		private set
	var minLength: Int = 0
		private set
	var storedLength: Int = 0
		private set
	var maxLength: Int? = null
		private set
	var passwordReference: Int? = null
		private set
	var padChar: Byte? = null
		private set
	var lastPasswordChange: TLV? = null
		private set
	var path: Path? = null
		private set

	init {
		val p = Parser(tlv.child)

		if (p.match(Tag.Companion.BITSTRING_TAG)) {
			passwordFlags = TLVBitString(p.next(0)!!)
		} else {
			throw TLVException("passwordFlags element missing.")
		}
		if (p.match(Tag.Companion.ENUMERATED_TAG)) {
			passwordType = toInteger(p.next(0)!!.value!!)
		} else {
			throw TLVException("passwordType element missing.")
		}
		if (p.match(Tag.Companion.INTEGER_TAG)) {
			minLength = toInteger(p.next(0)!!.value!!)
		} else {
			throw TLVException("minLength element missing.")
		}
		if (p.match(Tag.Companion.INTEGER_TAG)) {
			storedLength = toInteger(p.next(0)!!.value!!)
		} else {
			throw TLVException("storedLength element missing.")
		}
		if (p.match(Tag.Companion.INTEGER_TAG)) {
			maxLength = toInteger(p.next(0)!!.value!!)
		}
		if (p.match(Tag(TagClass.CONTEXT, true, 0))) {
			passwordReference = toInteger(p.next(0)!!.value!!)
		}
		if (p.match(Tag.Companion.OCTETSTRING_TAG)) {
			padChar = p.next(0)!!.value?.get(0)
		}
		if (p.match(Tag(TagClass.UNIVERSAL, true, 24))) {
			lastPasswordChange = p.next(0)
		}
		if (p.match(Tag.Companion.SEQUENCE_TAG)) {
			path = Path(p.next(0)!!)
		}
	}
}
