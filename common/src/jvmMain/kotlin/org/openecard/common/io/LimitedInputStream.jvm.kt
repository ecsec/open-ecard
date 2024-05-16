/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.common.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Implements an input stream with a limit.
 *
 * @author Moritz Horsch
 */
class LimitedInputStream
/**
 * Creates a new limited input stream.
 *
 * @param inputStream Input stream
 */
@JvmOverloads constructor(inputStream: InputStream, private var limit: Int = 1048576) :
	FilterInputStream(inputStream) {
    /**
     * Creates a new limited input stream.
     *
     * @param inputStream Input stream
     * @param limit Limit
     */
    @Throws(IOException::class)
    override fun read(): Int {
        val res = super.read()
        if (res != -1) {
            limit--
            checkLimit()
        }
        return res
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return this.read(b, 0, b.size)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val res = super.read(b, off, len)
        if (res != -1) {
            limit -= len
            checkLimit()
        }
        return res
    }

    /**
     * Checks if the limit of the stream is reached.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun checkLimit() {
        if (limit < 1) {
            throw IOException("Input streams limit is reached.")
        }
    }
}
