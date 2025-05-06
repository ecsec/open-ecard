/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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
package org.openecard.sal.protocol.eac.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.common.anytype.AuthDataMap

/**
 * Implements the EACAdditionalInputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.7.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 */
class EACAdditionalInputType(
	baseType: DIDAuthenticationDataType,
) {
	private val authMap: AuthDataMap = AuthDataMap(baseType)

	/**
	 * Returns the signature.
	 *
	 * @return Signature
	 */
	val signature: ByteArray? = authMap.getContentAsBytes(SIGNATURE)

	val outputType: EAC2OutputType
		/**
		 * Returns EAC1OutputType.
		 *
		 * @return EAC1OutputType
		 */
		get() = EAC2OutputType(authMap)

	companion object {
		const val SIGNATURE: String = "Signature"
	}
}
