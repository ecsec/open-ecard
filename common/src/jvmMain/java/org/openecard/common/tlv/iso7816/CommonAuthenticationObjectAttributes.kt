/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

/**
 *
 * @author Hans-Martin Haase
 */
class CommonAuthenticationObjectAttributes(
	tlv: TLV,
) : TLVType(tlv) {
	var authId: ByteArray? = null
		private set
	private var authReference: Int? = null
	private var securityEnvironmentID: Int? = null

	init {
		val p = Parser(tlv.child)
		if (p.match(Tag(TagClass.UNIVERSAL, true, 4))) {
			authId = p.next(0)!!.value
		}

		if (p.match(Tag(TagClass.UNIVERSAL, true, 2))) {
			authReference = toInteger(p.next(0)!!.value!!)
		}

		if (p.match(Tag(TagClass.CONTEXT, true, 0))) {
			securityEnvironmentID = toInteger(p.next(0)!!.value!!)
		}
	}

	fun getAuthReference(): Int = authReference ?: -1

	fun getSecurityEnvironmentID(): Int = securityEnvironmentID ?: -1
}
