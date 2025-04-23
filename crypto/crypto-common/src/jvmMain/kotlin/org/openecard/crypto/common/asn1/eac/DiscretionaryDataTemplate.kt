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
import org.openecard.common.tlv.*
import org.openecard.common.tlv.iso7816.TLVList
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils

/**
 * Data object for the DiscretionaryDataTemplate described in BSI TR-03110-3 Sec. A.6.5.1.
 *
 * @author Tobias Wich
 */
class DiscretionaryDataTemplate(
	tlv: TLV,
) : TLVList(tlv, Tag(TagClass.APPLICATION, false, EACTags.DISCRETIONARY_DATA_OBJECTS.toLong())) {
	val objectIdentifier: ASN1ObjectIdentifier
	private val data: ByteArray

	init {
		val p = Parser(tlv.getChild())
		if (p.match(Tag.OID_TAG)) {
			try {
				val oidStr = ObjectIdentifierUtils.toString(p.next(0).value)
				objectIdentifier = ASN1ObjectIdentifier(oidStr)
			} catch (ex: IllegalArgumentException) {
				throw TLVException(ex)
			}
		} else {
			throw TLVException("Object Identifier is missing in DiscretionaryDataTemplate.")
		}
		if (p.match(Tag(TagClass.APPLICATION, true, EACTags.DISCRETIONARY_DATA.toLong()))) {
			data = p.next(0).value
		} else {
			throw TLVException("Discretionary Data is missing in DiscretionaryDataTemplate.")
		}
	}

	val discretionaryData: ByteArray
		get() = ByteUtils.clone(data)
}
