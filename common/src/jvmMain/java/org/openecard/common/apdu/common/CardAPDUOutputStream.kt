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
package org.openecard.common.apdu.common

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 *
 * @author Moritz Horsch
 */
class CardAPDUOutputStream : ByteArrayOutputStream {
    /**
     * Creates a new byte array stream for APDUs.
     */
    constructor() : super()

    /**
     * Creates a new byte array stream for APDUs, with a buffer capacity of the specified size.
     *
     * @param size Initial size.
     */
    constructor(size: Int) : super(size)

    /**
     * Writes TLV encoded data to the stream.
     *
     * @param type Type
     * @param value Value
     * @throws IOException if an I/O error occurs
     */

    fun writeTLV(type: Byte, value: Byte) {
        write(type.toInt())
        write(0x01.toByte().toInt())
        write(value.toInt())
    }

    /**
     * Writes TLV encoded data to the stream.
     *
     * @param type Type
     * @param value Value
     * @throws IOException if an I/O error occurs
     */

    fun writeTLV(type: Byte, value: ByteArray) {
        val length = value.size

        write(type.toInt())
        if (length in 0x80..0xFF) {
            write(0x81.toByte().toInt())
        } else if (length == 0xFF) {
            write(0x00.toByte().toInt())
        } else if (length > 0xFF) {
            write(0x82.toByte().toInt())
        }
        write(toByteArray())
        write(value)
    }
}
