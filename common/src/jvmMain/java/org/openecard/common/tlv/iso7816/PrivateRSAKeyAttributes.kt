/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

/**
 *
 * @author Hans-Martin Haase
 */
class PrivateRSAKeyAttributes
	@Throws(TLVException::class)
	constructor(
		tlv: TLV,
	) : TLVType(tlv) {
		var value: Path? = null
			private set
		var modulusLength: ByteArray?
			private set

		// KeyInfo sequence
		var parameters: TLV? = null // NULL, so not interesting
			private set
		var operations: TLVBitString? = null
			private set

		init {
			val p = Parser(tlv.child)

			if (p.match(Tag.SEQUENCE_TAG)) {
				value = Path(p.next(0)!!)
			} else {
				throw TLVException("No value element in structure.")
			}
			if (p.match(Tag.INTEGER_TAG)) {
				modulusLength = p.next(0)!!.value
			} else {
				throw TLVException("No modulusLength element in structure.")
			}
			// only match sequence not Reference (historical)
			if (p.match(Tag.SEQUENCE_TAG)) {
				val p1 = Parser(p.next(0)!!.child)
				if (p1.match(Tag.NULL_TAG)) {
					parameters = p1.next(0)
				} else {
					throw TLVException("No parameters element in structure.")
				}
				if (p1.match(Tag(TagClass.UNIVERSAL, true, 3))) {
					operations = TLVBitString(p1.next(0)!!, Tag(TagClass.UNIVERSAL, true, 3).tagNumWithClass)
				}
			}
		}
	}
