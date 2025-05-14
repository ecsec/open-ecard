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
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass
import org.openecard.common.util.ByteUtils.toInteger
import java.nio.charset.Charset

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class CommonObjectAttributes(
	tlv: TLV,
) : TLVType(tlv) {
	var label: String? = null
		private set
	var flags: TLVBitString? = null
		private set
	var authId: ByteArray? = null
		private set
	private var userConsent: Int? = null // 1..15
	var aCLS: List<TLV?>? = null
		private set

	init {
		val p = Parser(tlv.child)

		if (p.match(Tag(TagClass.UNIVERSAL, true, 12))) {
			label = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		if (p.match(Tag(TagClass.UNIVERSAL, true, 3))) {
			flags = TLVBitString(p.next(0)!!, Tag(TagClass.UNIVERSAL, true, 3).tagNumWithClass)
		}
		if (p.match(Tag(TagClass.UNIVERSAL, true, 4))) {
			authId = p.next(0)?.value
		}
		if (p.match(Tag.INTEGER_TAG)) {
			userConsent = toInteger(p.next(0)?.value!!)
		}
		if (p.match(Tag.SEQUENCE_TAG)) {
			val list = TLVList(p.next(0)!!)
			aCLS = list.content
		}
	}

	fun getUserConsent(): Int = userConsent!!
}
