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
package org.openecard.sal.protocol.eac.apdu

import org.openecard.common.apdu.ManageSecurityEnvironment
import org.openecard.common.apdu.common.CardAPDUOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Implements a MSE:Set DST APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.4.
 *
 * @author Moritz Horsch
 */
class MSESetDST : ManageSecurityEnvironment {
    /**
     * Creates a new MSE:Set DST APDU.
     */
    constructor() : super(0x81.toByte(), DST)

    /**
     * Creates a new MSE:Set DST APDU.
     *
     * @param chr Certificate Holder Reference
     */
    constructor(chr: ByteArray) : super(0x81.toByte(), DST) {
        val caos = CardAPDUOutputStream()
        try {
            caos.writeTLV(0x83.toByte(), chr)

            caos.flush()
        } catch (e: IOException) {
            logger.error(e.message, e)
        } finally {
            try {
                caos.close()
            } catch (ignore: IOException) {
            }
        }

        setData(caos.toByteArray())
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MSESetATCA::class.java)
    }
}
