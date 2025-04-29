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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardAPDU
import org.openecard.common.apdu.common.CardCommandAPDU

/**
 * EXTERNAL AUTHENTICATION Command
 * See ISO/IEC 7816-4 Section 7.5.4
 *
 * @author Moritz Horsch
 */
class ExternalAuthentication(data: ByteArray?) : CardCommandAPDU(
    CardAPDU.Companion.x00,
    EXTERNAL_AUTHENTICATION_INS,
    CardAPDU.Companion.x00,
    CardAPDU.Companion.x00
) {
    /**
     * Creates a new External Authenticate APDU.
     * APDU: 0x00 0x82 0x00 0x00 LC DATA
     *
     * @param data Data
     */
    init {
        setData(data!!)
    }

    companion object {
        /**
         * EXTERNAL AUTHENTICATION command instruction byte
         */
        private const val EXTERNAL_AUTHENTICATION_INS = 0x82.toByte()
    }
}
