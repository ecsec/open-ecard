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
import org.openecard.common.tlv.TLV
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * GENERAL AUTHENTICATION Command
 * See ISO/IEC 7816-4 Section 7.5.2
 *
 * @author Moritz Horsch
 */
class GeneralAuthenticate : CardCommandAPDU {
    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 0x02 0x7C 0x00 0x00
     */
    constructor() : super(
        CardAPDU.Companion.x00,
        GENERAL_AUTHENTICATION_INS,
        CardAPDU.Companion.x00,
        CardAPDU.Companion.x00
    ) {
        setLC(0x02.toByte())
        setData(byteArrayOf(0x7C.toByte(), CardAPDU.Companion.x00))
        le = CardAPDU.Companion.x00
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x10 0x86 0x00 0x00 LC 0x7C DATA 0x00
     *
     * @param data Data
     */
    constructor(data: ByteArray?) : super(
        CardAPDU.Companion.x00,
        GENERAL_AUTHENTICATION_INS,
        CardAPDU.Companion.x00,
        CardAPDU.Companion.x00
    ) {
        setData(data!!)
        le = CardAPDU.Companion.x00
    }

    /**
     * Creates a new GENERAL AUTHENTICATION APDU.
     * APDU: 0x00 0x86 0x00 0x00 LC 0x7C DATA 0x00
     * Tag should be one of:
     * '80' Witness
     * '81' Challenge
     * '82' Response
     * '83' Committed challenge
     * '84' Authentication code
     * '85' Exponential
     * 'A0' Identification data template
     *
     * @param tag Authentication data tag. 0x7C is omitted!
     * @param authData Authentication data
     */
    constructor(tag: Byte, authData: ByteArray?) : super(
        CardAPDU.Companion.x00,
        GENERAL_AUTHENTICATION_INS,
        CardAPDU.Companion.x00,
        CardAPDU.Companion.x00
    ) {
        try {
            val tag7c = TLV()
            val tagData = TLV()

            tag7c.setTagNumWithClass(0x7C.toByte())
            tag7c.child = tagData
            tagData.setTagNumWithClass(tag)
            tagData.value = authData

            setData(tag7c.toBER())
        } catch (ex: Exception) {
            _logger.error(ex.message, ex)
        }

        le = CardAPDU.Companion.x00
    }

    companion object {
        private val _logger: Logger = LoggerFactory.getLogger(GeneralAuthenticate::class.java)

        /**
         * GENERAL AUTHENTICATION command instruction byte
         */
        private const val GENERAL_AUTHENTICATION_INS = 0x86.toByte()
    }
}
