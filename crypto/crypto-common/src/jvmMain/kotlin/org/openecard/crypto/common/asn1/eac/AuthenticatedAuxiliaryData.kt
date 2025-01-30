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
 ***************************************************************************/
package org.openecard.crypto.common.asn1.eac

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.eac.EACTags
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.Tag
import org.openecard.common.tlv.TagClass
import org.openecard.common.tlv.iso7816.TLVList
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier
import java.util.*

/**
 * Data object for the AuthenticatedAuxiliaryData according to TR-03110-3 Sec. A.6.5.
 *
 * @author Tobias Wich
 */
class AuthenticatedAuxiliaryData(tlv: TLV) :
	TLVList(tlv, Tag(TagClass.APPLICATION, false, EACTags.AUTHENTIFICATION_DATA.toLong())) {
	private val templates: HashMap<ASN1ObjectIdentifier, DiscretionaryDataTemplate> = HashMap()
	private var empty = false

	init {
		// convert content to DiscretionaryDataTemplates
		val ts = ArrayList<DiscretionaryDataTemplate>()
		for (next in getContent()) {
			val d = DiscretionaryDataTemplate(next)
			ts.add(d)
		}
		// Fill map by looking at the objects in reverse order, because the TR states only the last element of a given
		// OID should be used.
		ts.reversed().forEach {
			val oid = it.objectIdentifier
			if (!templates.containsKey(oid)) {
				templates.put(oid, it)
			}
		}
	}

	constructor(data: ByteArray?) : this(emptyOrStructure(data)) {
		if (data == null) {
			empty = true
		}
	}

	val data: ByteArray?
		get() = if (empty) null else this.tlv.toBER()


	val ageVerificationData: Calendar?
		get() {
			val reqOID = ASN1ObjectIdentifier(EACObjectIdentifier.id_DateOfBirth)
			return templates.get(reqOID)?.let { convertDate(it.discretionaryData) }
		}

	val documentValidityVerificationData: Calendar?
		get() {
			val reqOID = ASN1ObjectIdentifier(EACObjectIdentifier.id_DateOfExpiry)
			if (templates.containsKey(reqOID)) {
				val t: DiscretionaryDataTemplate = templates.get(reqOID)!!
				return convertDate(t.discretionaryData)
			} else {
				return null
			}
		}

	val communityIDVerificationData: ByteArray?
		get() {
			val reqOID =
				ASN1ObjectIdentifier(EACObjectIdentifier.id_CommunityID)
			if (templates.containsKey(reqOID)) {
				val t: DiscretionaryDataTemplate = templates.get(reqOID)!!
				return ByteUtils.clone(t.discretionaryData)
			} else {
				return null
			}
		}

	private fun convertDate(discretionaryData: ByteArray): Calendar {
		require(discretionaryData.size == 8) { "Given value in discretionary data does not represent a date." }
		// the date is in the form YYYYMMDD and encoded as characters
		val yearStr = String(discretionaryData.copyOfRange(0, 4))
		val year = yearStr.toInt()
		val monthStr = String(discretionaryData.copyOfRange(4, 6))
		val month = monthStr.toInt()
		val dayStr = String(discretionaryData.copyOfRange(6, 8))
		val day = dayStr.toInt()
		// convert to Date object
		val c = Calendar.getInstance()
		c.set(year, month, day)
		return c
	}

}

@Throws(TLVException::class)
private fun emptyOrStructure(data: ByteArray?): TLV {
	if (data == null) {
		val tlv = TLV()
		tlv.tag = Tag(TagClass.APPLICATION, false, EACTags.AUTHENTIFICATION_DATA.toLong())
		return tlv
	} else {
		return TLV.fromBER(data)
	}
}
