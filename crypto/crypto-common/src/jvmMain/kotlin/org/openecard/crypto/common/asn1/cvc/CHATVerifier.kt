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

import org.openecard.common.util.ByteUtils
import java.security.GeneralSecurityException

/**
 *
 * @author Moritz Horsch
 */
object CHATVerifier {
    /**
     * Verifies that the second CHAT is a subset of the first one.
     * Ensures that the second CHAT does not have move rights then the first one.
     *
     * @param firstCHAT First CHAT
     * @param secondCHAT Second CHAT
     * @throws GeneralSecurityException
     */
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun verfiy(firstCHAT: CHAT, secondCHAT: CHAT) {
        try {
            val firstCHATBytes = firstCHAT.toByteArray()
            val secondCHATBytes = secondCHAT.toByteArray()

            for (i in 0..<firstCHATBytes.size * 8) {
                if (ByteUtils.isBitSet(i, secondCHATBytes) && !ByteUtils.isBitSet(i, firstCHATBytes)) {
                    throw GeneralSecurityException("The second CHAT is not a subset of the first one")
                }
            }
        } catch (e: Exception) {
            throw GeneralSecurityException(e)
        }
    }
}
