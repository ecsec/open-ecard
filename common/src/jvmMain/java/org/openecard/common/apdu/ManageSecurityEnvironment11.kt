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
 * MANAGE SECURITY ENVIRONMENT command.
 * See ISO/IEC 7816-4 Section 7.5.11.
 *
 * @author Moritz Horsch
 */
open class ManageSecurityEnvironment : CardCommandAPDU {
    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    protected constructor(p1: Byte, p2: Byte) : super(CardAPDU.Companion.x00, COMMMAND_MSESet_AT, p1, p2)

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data
     */
    constructor(p1: Byte, p2: Byte, data: ByteArray?) : super(CardAPDU.Companion.x00, COMMMAND_MSESet_AT, p1, p2) {
        setData(data!!)
    }

    open class Set(p1: Byte, p2: Byte) : ManageSecurityEnvironment(p1, p2)

    class Store(p2: Byte) : ManageSecurityEnvironment(0xF2.toByte(), p2)

    class Restore(p2: Byte) : ManageSecurityEnvironment(0xF3.toByte(), p2)

    class Erase(p2: Byte) : ManageSecurityEnvironment(0xF4.toByte(), p2)

    companion object {
        /** MANAGE SECURITY ENVIRONMENT command instruction byte.  */
        private const val COMMMAND_MSESet_AT = 0x22.toByte()

        /** Control reference template for authentication.  */
        const val AT: Byte = 0xA4.toByte()

        /** Control reference template for key agreement.  */
        const val KAT: Byte = 0xA6.toByte()

        /** Control reference template for hash-code.  */
        const val HT: Byte = 0xAA.toByte()

        /** Control reference template for cryptographic checksum.  */
        const val CCT: Byte = 0xB4.toByte()

        /** Control reference template for digital signature.  */
        const val DST: Byte = 0xB6.toByte()

        /** Control reference template for confidentiality.  */
        const val CT: Byte = 0xB8.toByte()
    }
}
