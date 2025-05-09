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

/**
 *
 * @author Tobias Wich
 */
class Certificate(
	tlv: TLV,
) : TLVType(tlv) {
	private var toBeSigned: TLV? = null // CertificateContent
	private var algorithmIdentifier: TLV? = null // AlgorithmIdentifier
	private var encrypted: TLVBitString? = null

	init {
		val p = Parser(tlv.child)

		if (p.match(SEQUENCE_TAG)) {
			toBeSigned = p.next(0)
		} else {
			throw TLVException("toBeSigned element missing.")
		}
		if (p.match(SEQUENCE_TAG)) {
			algorithmIdentifier = p.next(0)
		} else {
			throw TLVException("algorithmIdentifier element missing.")
		}
		if (p.match(Tag.BITSTRING_TAG)) {
			encrypted = TLVBitString(p.next(0)!!)
		} else {
			throw TLVException("encrypted element missing.")
		}
	}
}
