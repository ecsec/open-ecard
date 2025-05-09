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
 ***************************************************************************/
package org.openecard.common.ifd.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.AuthDataResponse

/**
 * Implements the PACEInputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.3.5.
 *
 * @author Tobias Wich
 */
class PACEInputType(
	baseType: DIDAuthenticationDataType,
) {
	//
	private val authMap: AuthDataMap = AuthDataMap(baseType)

	/**
	 * Returns the PIN.
	 *
	 * @return PIN
	 */
	val pIN: String? = authMap.getContentAsString(PIN)

	/**
	 * Returns the PIN ID.
	 *
	 * @return PIN ID
	 */
	val pINID: Byte = authMap.getContentAsBytes(PIN_ID)!![0]

	// optional elements

	/**
	 * Returns the CHAT.
	 *
	 * @return CHAT
	 */
	val cHAT: ByteArray? = authMap.getContentAsBytes(CHAT)

	/**
	 * Returns the certificate description.
	 *
	 * @return Certificate description
	 */
	val certificateDescription: ByteArray? = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION)
	val isUseShortEf: Boolean = authMap.getAttribute(AuthDataResponse.OEC_NS, USE_SHORT_EF).toBoolean()

	val outputType: PACEOutputType
		/**
		 * Returns a PACEOutputType based on the PACEInputType.
		 *
		 * @return PACEOutputType
		 */
		get() = PACEOutputType(authMap)

	companion object {
		const val PIN_ID: String = "PinID"
		const val CHAT: String = "CHAT"
		const val PIN: String = "PIN"
		const val CERTIFICATE_DESCRIPTION: String = "CertificateDescription"
		const val USE_SHORT_EF: String = "UseShortEF"
	}
}
