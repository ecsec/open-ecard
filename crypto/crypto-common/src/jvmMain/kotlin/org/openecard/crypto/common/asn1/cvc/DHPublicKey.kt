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
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.tlv.TLV

/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.2.
 *
 * @author Moritz Horsch
 */
class DHPublicKey protected constructor(tlv: TLV) : PublicKey() {
    /**
     * Creates a new DHPublicKey.
     *
     * @param tlv TLV encoded key
     */
	init {
		throw UnsupportedOperationException("Not supported yet.")
	}

	override val objectIdentifier: String
		get() = throw UnsupportedOperationException("Not supported yet.")

	override val tLVEncoded: TLV
		get() = throw UnsupportedOperationException("Not supported yet.")

	override fun compare(pk: PublicKey): Boolean {
		throw UnsupportedOperationException("Not supported yet.")
	}
}
