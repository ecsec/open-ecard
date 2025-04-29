/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
 * DELETE FILE command.
 * See ISO/IEC 7816-9 Section 9.3.3.
 *
 * @author Hans-Martin Haase
 */
open class DeleteFile(p1: Byte, p2: Byte) :
    CardCommandAPDU(CardAPDU.Companion.x00, INS_DELETE_FILE, p1, p2) {
    fun setFCI() {
        p2 = FCI
    }

    fun setFCP() {
        p2 = FCP
    }

    fun setFMD() {
        p2 = FMD
    }

    /**
     * Implements a DELETE FILE APDU to delete the Master File.
     */
    class MasterFile : DeleteFile(0x00.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete the Master File.
         */
        init {
            setData(MF_FID)
        }

        companion object {
            /**
             * MasterFile file identifier .
             */
            val MF_FID: ByteArray = byteArrayOf(0x3F.toByte(), 0x00.toByte())
        }
    }

    /**
     * Implements a DELETE FILE APDU to delete a file (DF or EF).
     */
    class File(fid: ByteArray?) : DeleteFile(0x00.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a file.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }
    }

    /**
     * Implements a DELETE FILE APDU to delete a directory (DF).
     */
    class ChildDirectory(fid: ByteArray?) : DeleteFile(0x01.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a directory.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }
    }

    /**
     * Implements a DELETE FILE APDU to delete a file (EF).
     */
    class ChildFile(fid: ByteArray?) : DeleteFile(0x02.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a file.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }

        /**
         * Creates a new DELETE FILE APDU to delete a file.
         *
         * @param fid ChildFile Identifier
         */
        constructor(fid: Short) : this(toByteArray(fid))
    }

    /**
     * Implements a DELETE FILE APDU to delete the parent directory.
     */
    class Parent
    /**
     * Creates a new DELETE FILE APDU to delete the parent directory.
     */
        : DeleteFile(0x03.toByte(), 0x0C.toByte())

    /**
     * Implements a DELETE FILE APDU to delete a application.
     */
    class Application(aid: ByteArray?) : DeleteFile(0x04.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a application.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    /**
     * Implements a DELETE FILE APDU to delete a absolute path.
     */
    class AbsolutePath(aid: ByteArray?) : DeleteFile(0x08.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a absolute path.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    /**
     * Implements a DELETE FILE APDU to delete a relative path.
     */
    class RelativePath(aid: ByteArray?) : DeleteFile(0x09.toByte(), 0x0C.toByte()) {
        /**
         * Creates a new DELETE FILE APDU to delete a relative path.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    companion object {
        /**
         * Instruction byte for the DELETE FILE command.
         */
        private const val INS_DELETE_FILE = 0xE4.toByte()

        /**
         * P2 value for FCI return type.
         */
        private const val FCI = 0x00.toByte()

        /**
         * P2 value for FCP return type.
         */
        private const val FCP = 0x04.toByte()

        /**
         * P2 value for FMD return type.
         */
        private const val FMD = 0x08.toByte()
    }
}
