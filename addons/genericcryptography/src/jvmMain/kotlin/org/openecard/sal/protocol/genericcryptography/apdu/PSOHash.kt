/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
package org.openecard.sal.protocol.genericcryptography.apdu

import org.openecard.common.apdu.PerformSecurityOperation

/**
 * Implements a Hash operation.
 * See ISO/IEC 7816-8, section 11.8.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class PSOHash(p2: Byte, data: ByteArray?) : PerformSecurityOperation(0x90.toByte(), p2) {
    /**
     * Creates a new PSO Hash APDU.
     * APDU: 0x00 0x2A 0x90 0x80|0xA0 Lc data
     *
     * @param data Data to be hashed or hash or parameters for the hash according to ISO7816-8 section 11.8.3.
     * @param p2 P2 value according to ISO7816-98 section 11.8.3. The class provides two public variables for this
     * purpose.
     */
    init {
        setData(data)
    }

    companion object {
        /**
         * P2 value for complete hash generation on the card.
         */
        val P2_HASH_MESSAGE: Byte = 0x80.toByte()

        /**
         * P2 value for setting a HashValue or specific parameters.
         */
        val P2_SET_HASH_OR_PART: Byte = 0xA0.toByte()
    }
}
