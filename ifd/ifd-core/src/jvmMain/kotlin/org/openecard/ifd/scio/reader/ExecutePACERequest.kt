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
package org.openecard.ifd.scio.reader

import org.openecard.common.util.ShortUtils
import java.io.ByteArrayOutputStream

/**
 *
 * @author Tobias Wich
 */
class ExecutePACERequest {
    enum class Function(internal val code: Byte) {
        GetReaderPACECapabilities(1.toByte()),
        EstablishPACEChannel(2.toByte()),
        DestroyPACEChannel(3.toByte())
    }


    constructor(f: Function) {
        this.function = f
		this.dataLength = 0
		this.data = null
    }

    constructor(f: Function, data: ByteArray) {
        this.function = f
        this.dataLength = data.size.toShort()
        this.data = data
    }


    private val function: Function
    private val dataLength: Short
    private val data: ByteArray?

    fun toBytes(): ByteArray {
        val o = ByteArrayOutputStream()
        o.write(function.code.toInt())
        // write data length
        val dataLengthBytes = ShortUtils.toByteArray(dataLength)
        for (i in dataLengthBytes.indices.reversed()) {
            o.write(dataLengthBytes[i].toInt())
        }
        // write missing bytes to length field
        for (i in dataLengthBytes.size..1) {
            o.write(0)
        }
        // write data if there is a positive length
        if (dataLength > 0) {
            o.write(data!!, 0, data.size)
        }

        return o.toByteArray()
    }
}
