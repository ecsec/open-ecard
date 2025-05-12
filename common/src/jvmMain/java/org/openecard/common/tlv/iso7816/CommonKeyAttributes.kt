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
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.Tag.Companion.BITSTRING_TAG
import org.openecard.common.tlv.Tag.Companion.BOOLEAN_TAG
import org.openecard.common.tlv.Tag.Companion.INTEGER_TAG
import org.openecard.common.tlv.Tag.Companion.OCTETSTRING_TAG
import org.openecard.common.tlv.TagClass
import org.openecard.common.util.ByteUtils.toInteger

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class CommonKeyAttributes
	@Throws(TLVException::class)
	constructor(
		tlv: TLV,
	) : TLVType(tlv) {
		var id: ByteArray?
			private set
		var usage: TLVBitString? = null
			private set
		var isNativeFlag: Boolean = true
			private set
		var accessFlags: TLVBitString? = null
			private set
		private var keyReference: Int? = null
		var startDate: TLV? = null
			private set
		var endDate: TLV? = null
			private set
		private var algRefs: MutableList<Int> = mutableListOf()

		init {
			val p = Parser(tlv.child)

			if (p.match(OCTETSTRING_TAG)) {
				id = p.next(0)!!.value
			} else {
				throw TLVException("No id element in structure.")
			}
			if (p.match(BITSTRING_TAG)) {
				usage = TLVBitString(p.next(0)!!)
			} else {
				throw TLVException("No usage element in structure.")
			}
			if (p.match(BOOLEAN_TAG)) {
				isNativeFlag = p
					.next(0)!!
					.value[0]
					.toInt() != 0x00
			}
			if (p.match(BITSTRING_TAG)) {
				accessFlags = TLVBitString(p.next(0)!!)
			}
			if (p.match(INTEGER_TAG)) {
				keyReference = toInteger(p.next(0)!!.value)
			}
			if (p.match(Tag(TagClass.UNIVERSAL, true, 24))) {
				startDate = p.next(0)
			}
			if (p.match(Tag(TagClass.CONTEXT, true, 0))) {
				endDate = p.next(0)
			}
			if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
				val list = TLVList(p.next(0)!!, Tag(TagClass.CONTEXT, false, 1).tagNumWithClass)
				for (next in list.content) {
					algRefs.add(toInteger(next.value))
				}
			}
		}

		fun getKeyReference(): Int = keyReference ?: -1

		fun getAlgRefs(): List<Int> = algRefs
	}
