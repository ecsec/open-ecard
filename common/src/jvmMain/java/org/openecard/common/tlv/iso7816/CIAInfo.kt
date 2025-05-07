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
import org.openecard.common.tlv.Tag.Companion.SEQUENCE_TAG
import org.openecard.common.tlv.TagClass
import org.openecard.common.util.ByteUtils.toLong
import java.nio.charset.Charset
import java.util.LinkedList

/**
 *
 * @author Tobias Wich
 */
class CIAInfo(
	tlv: TLV,
) : TLV(tlv) {
	var version: Long = 0
		private set
	var serialNumber: ByteArray?
		private set
	var manufacturerID: String?
		private set
	var label: String?
		private set
	var cardFlags: CardFlags? = null
		private set
	var seInfo: List<TLV?>
		private set
	var recordInfo: TLV?
		private set
	var supportedAlgorithms: List<TLV?>
		private set
	var issuerId: String?
		private set
	var holderId: String?
		private set
	var lastUpdate: TLV?
		private set
	var preferredLanguage: String?
		private set
	var profileIndication: List<TLV?>
		private set

	init {
		if (tlv.tagNumWithClass != SEQUENCE_TAG.tagNumWithClass) {
			throw TLVException("Data doesn't represent a CIAInfo structure.")
		}

		val p = Parser(tlv.child)
		// version
		if (p.match(Tag(TagClass.UNIVERSAL, true, 2))) {
			version = toLong(p.next(0)?.value!!)
		} else {
			throw TLVException("Expected version tag.")
		}
		// serialNumber
		serialNumber = null
		if (p.match(Tag(TagClass.UNIVERSAL, true, 4))) {
			serialNumber = p.next(0)?.value
		}
		// manufacturer ID
		manufacturerID = null
		if (p.match(Tag(TagClass.UNIVERSAL, true, 12))) {
			manufacturerID = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		// label
		label = null
		if (p.match(Tag(TagClass.CONTEXT, true, 0))) {
			label = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		// cardflags
		if (p.match(Tag(TagClass.UNIVERSAL, true, 3))) {
			cardFlags = CardFlags(p.next(0)!!)
		} else {
			throw TLVException("Expected cardflags tag.")
		}
		// seInfo
		seInfo = LinkedList()
		if (p.match(Tag(TagClass.UNIVERSAL, false, 16))) {
			seInfo = TLVList(p.next(0)!!, Tag(TagClass.UNIVERSAL, false, 16).tagNumWithClass).content
		}
		// recordInfo
		recordInfo = null
		if (p.match(Tag(TagClass.CONTEXT, false, 1))) {
			recordInfo = p.next(0)
		}
		// supportedAlgorithms
		supportedAlgorithms = LinkedList()
		if (p.match(Tag(TagClass.CONTEXT, false, 2))) {
			supportedAlgorithms =
				TLVList(p.next(0)!!.child!!, SEQUENCE_TAG.tagNumWithClass).content
		}
		// issuerId
		issuerId = null
		if (p.match(Tag(TagClass.CONTEXT, true, 3))) {
			issuerId = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		// holderId
		holderId = null
		if (p.match(Tag(TagClass.CONTEXT, true, 4))) {
			holderId = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		// lastUpdate
		lastUpdate = null
		if (p.match(Tag(TagClass.CONTEXT, false, 5))) {
			lastUpdate = p.next(0)
		}
		// preferredLanguage
		preferredLanguage = null
		if (p.match(Tag(TagClass.UNIVERSAL, true, 19))) {
			preferredLanguage = String(p.next(0)?.value!!, Charset.forName("UTF-8"))
		}
		// profileIndication
		profileIndication = LinkedList()
		if (p.match(Tag(TagClass.CONTEXT, false, 6))) {
			profileIndication = TLVList(p.next(0)!!, Tag(TagClass.CONTEXT, false, 6).tagNumWithClass).content
		}
	}
}
