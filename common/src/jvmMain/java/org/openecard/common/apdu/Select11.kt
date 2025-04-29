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
 *
 * @author Moritz Horsch
 * @author Johannes Schmoelz
 * @author Tobias Wich
 */
open class Select
/**
 * Creates a new Select APDU.
 *
 * @param p1 Parameter byte P1
 * @param p2 Parameter byte P2
 */
    (p1: Byte, p2: Byte) :
    CardCommandAPDU(CardAPDU.Companion.x00, SELECT_INS, p1, p2) {
    /**
     * Set to return no file metadata (FCI, FCP, FMD).
     */
    fun setNoMetadata() {
        p2 = NO_FILE_METADATA
        le = 0
    }

    /**
     * Set to return FCI (File Control Information) template.
     */
    fun setFCI() {
        p2 = FCI
        le = CardAPDU.Companion.xFF
    }

    /**
     * Set to return FCP (File Control Parameters) template.
     */
    fun setFCP() {
        p2 = FCP
        le = CardAPDU.Companion.xFF
    }

    /**
     * Set to return FMD (File Management Data) template.
     */
    fun setFMD() {
        p2 = FMD
        le = CardAPDU.Companion.xFF
    }

    /**
     * Implements a Select APDU to select the Master File.
     */
    class MasterFile : Select(0x00.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select the Master File.
         */
        init {
            setData(MF_FID)
        }

        companion object {
            /** MasterFile file identifier  */
            val MF_FID: ByteArray = byteArrayOf(0x3F.toByte(), 0x00.toByte())
        }
    }

    /**
     * Implements a Select APDU to select a file (DF or EF).
     */
    class File(fid: ByteArray?) : Select(0x00.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a file.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }
    }

    /**
     * Implements a Select APDU to select a directory (DF).
     */
    class ChildDirectory(fid: ByteArray?) : Select(0x01.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a directory.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }
    }

    /**
     * Implements a Select APDU to select a file (EF).
     */
    class ChildFile(fid: ByteArray?) : Select(0x02.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a file.
         *
         * @param fid File Identifier
         */
        init {
            setData(fid!!)
        }

        /**
         * Creates a new Select APDU to select a file.
         *
         * @param fid ChildFile Identifier
         */
        constructor(fid: Short) : this(toByteArray(fid))
    }

    /**
     * Implements a Select APDU to select the parent directory.
     */
    class Parent
    /**
     * Creates a new Select APDU to select the parent directory.
     */
        : Select(0x03.toByte(), NO_FILE_METADATA)

    /**
     * Implements a Select APDU to select a application.
     */
    class Application(aid: ByteArray?) : Select(0x04.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a application.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    /**
     * Implements a Select APDU to select a absolute path.
     */
    class AbsolutePath(aid: ByteArray?) : Select(0x08.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a absolute path.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    /**
     * Implements a Select APDU to select a relative path.
     */
    class RelativePath(aid: ByteArray?) : Select(0x09.toByte(), NO_FILE_METADATA) {
        /**
         * Creates a new Select APDU to select a relative path.
         *
         * @param aid Application Identifier
         */
        init {
            setData(aid!!)
        }
    }

    companion object {
        /**
         * SELECT command instruction byte
         */
        private const val SELECT_INS = 0xA4.toByte()
        private const val NO_FILE_METADATA = 0x0C.toByte()
        private const val FCI = 0x00.toByte()
        private const val FCP = 0x04.toByte()
        private const val FMD = 0x08.toByte()
    }
}
