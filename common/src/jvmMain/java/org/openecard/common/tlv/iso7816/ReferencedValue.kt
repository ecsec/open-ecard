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
import org.openecard.common.tlv.TagClass

/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class ReferencedValue(
	tlv: TLV?,
) : TLVType(tlv) {
	var path: Path? = null
		private set
	var uRL: TLV? = null
		private set

	init {
		val p = Parser(tlv)

		if (p.match(Tag.Companion.SEQUENCE_TAG)) {
			path = Path(p.next(0)!!)
		} else if (p.match(Tag(TagClass.UNIVERSAL, true, 19)) ||
			p.match(Tag(TagClass.UNIVERSAL, true, 22)) ||
			p.match(Tag(TagClass.CONTEXT, false, 3))
		) {
			uRL = p.next(0) // TODO: create URL type
		} else {
			throw TLVException("Unexpected element in ObjectValue.")
		}
	}
}
