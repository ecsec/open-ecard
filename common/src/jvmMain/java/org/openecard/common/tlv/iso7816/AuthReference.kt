/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import org.openecard.common.util.ByteUtils.toInteger

/**
 * The class models the AuthReference data type from ISO7816-15.
 *
 * @author Hans-Martin Haase
 */
class AuthReference(
	tlv: TLV,
) : TLVType(tlv) {
	/**
	 * Gets the bit string which encodes the authentication method.
	 *
	 * @return A TLVBitString coding the authentication method.

	 * The authentication methods coded as [TLVBitString].
	 */
	var authMethod: TLVBitString? = null
		private set

	/**
	 * Gets the value of the securityEnvironment property.
	 *
	 * @return The necessary Security Environment identifier as integer value.

	 * An id of a security environment which have to be set while authentication.
	 */
	var securityEnvironmentId: Int? = null
		private set

	/**
	 * The constructor parses the input TLV and extracts the data structures.
	 *
	 * @param tlv The TLV containing the AuthReference data structure from ISO7816-15.
	 * @throws TLVException
	 */
	init {
		val p = Parser(tlv.child!!)

		if (p.match(Tag(TagClass.UNIVERSAL, true, 3))) {
			authMethod = TLVBitString(p.next(0)!!)
		} else {
			throw TLVException("No auth method object in the tlv available")
		}

		if (p.match(Tag(TagClass.UNIVERSAL, true, 2))) {
			securityEnvironmentId = toInteger(p.next(0)!!.value)
		}
	}
}
