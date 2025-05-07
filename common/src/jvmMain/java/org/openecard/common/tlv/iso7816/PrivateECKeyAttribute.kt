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

/**
 *
 * @author Tobias Wich
 */
class PrivateECKeyAttribute(
	tlv: TLV,
) : TLVType(tlv) {
	private var value: Path? = null

	// KeyInfo sequence
	// ECDomainParameters - Choice
	private var implicitCA: TLV? = null // NULL
	private var named: TLV? = null // ObjectIdentifier
	private var specified: TLV? = null // SpecifiedECDomain

	// PublicKeyOperations alias for Operations
	private var operations: TLVBitString? = null

	init {
		val p = Parser(tlv.child)

		if (p.match(Tag.Companion.SEQUENCE_TAG)) {
			value = Path(p.next(0)!!)
		} else {
			throw TLVException("No value element in structure.")
		}
		// only match sequence not Reference (historical)
		if (p.match(Tag.Companion.SEQUENCE_TAG)) {
			val p1 = Parser(p.next(0)!!.child)
			if (p1.match(Tag(TagClass.UNIVERSAL, true, 5))) {
				implicitCA = p1.next(0)
			} else if (p1.match(Tag(TagClass.UNIVERSAL, true, 6))) {
				named = p1.next(0)
			} else if (p1.match(Tag.Companion.SEQUENCE_TAG)) {
				specified = p1.next(0)
			} else {
				throw TLVException("No parameters element in structure.")
			}
			if (p1.match(Tag(TagClass.UNIVERSAL, true, 3))) {
				operations = TLVBitString(p1.next(0)!!, Tag(TagClass.UNIVERSAL, true, 3).tagNumWithClass)
			}
		}
	}
}
