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

import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import org.openecard.common.util.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

/**
 * Implements a response APDU.
 * See ISO/IEC 7816-4 Section 5.1.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class CardResponseAPDU : CardAPDU {
    private var trailer: ByteArray

    /**
     * Creates a new response APDU.
     */
    constructor() {
        trailer = ByteArray(2)
    }

    /**
     * Creates a new response APDU.
     *
     * @param responseAPDU Response APDU
     */
    constructor(responseAPDU: ByteArray) {
        trailer = ByteArray(2)
        data = ByteArray(responseAPDU.size - 2)

        System.arraycopy(responseAPDU, 0, data, 0, responseAPDU.size - 2)
        System.arraycopy(responseAPDU, responseAPDU.size - 2, trailer, 0, 2)
    }

    /**
     * Creates a new response APDU.
     *
     * @param data Data field
     * @param trailer Trailer (SW1, SW2)
     */
    constructor(data: ByteArray?, trailer: ByteArray) {
        setData(data!!)
        setTrailer(trailer)
    }

    /**
     * Creates a new response APDU.
     *
     * @param transmitResponse TransmitResponse
     */
    constructor(transmitResponse: TransmitResponse) : this(transmitResponse.outputAPDU[0])

    /**
     * Sets the trailer (status bytes) of the APDU.
     *
     * @param trailer Trailer (SW1, SW2)
     */
    fun setTrailer(trailer: ByteArray) {
        require(trailer.size == 2) { "Given trailer is not exactly two bytes long." }
        System.arraycopy(trailer, 0, this.trailer, 0, 2)
    }

    /**
     * Returns the trailer (status bytes) of the APDU.
     *
     * @return Trailer (SW1, SW2)
     */
    fun getTrailer(): ByteArray {
        return trailer
    }

    var sW1: Byte
        /**
         * Returns the status byte SW1.
         *
         * @return SW1
         */
        get() = (trailer[0].toInt() and 0xFF).toByte()
        /**
         * Sets the status byte SW1.
         *
         * @param sw1 SW1
         */
        protected set(sw1) {
            trailer[0] = sw1
        }

    var sW2: Byte
        /**
         * Returns the status byte SW1.
         *
         * @return SW2
         */
        get() = (trailer[1].toInt() and 0xFF).toByte()
        /**
         * Sets the status byte SW2.
         *
         * @param sw2 SW2
         */
        protected set(sw2) {
            trailer[1] = sw2
        }

    val sW: Short
        /**
         * Returns the status bytes of the APDU.
         *
         * @return Status bytes
         */
        get() = (((trailer[0].toInt() and 0xFF) shl 8) or (trailer[1].toInt() and 0xFF)).toShort()

    var statusBytes: ByteArray
        /**
         * Returns the status bytes of the APDU.
         *
         * @return Status bytes
         */
        get() = getTrailer()
        /**
         * Sets the status bytes of the APDU.
         *
         * @param statusbytes Status bytes
         */
        protected set(statusbytes) {
            setTrailer(statusbytes)
        }

    val statusMessage: String
        /**
         * Returns the status message of the APDU.
         *
         * @return Status bytes
         */
        get() = CardCommandStatus.getMessage(getTrailer())

    val isNormalProcessed: Boolean
        /**
         * Checks if the status bytes indicates an normal processing.
         * See ISO/IEC 7816-4 Section 5.1.3
         *
         * @return True if SW = 0x9000, otherwise false
         */
        get() {
            return if (trailer.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                true
            } else {
                false
            }
        }

    val isWarningProcessed: Boolean
        /**
         * Checks if the status bytes indicates a warning processing.
         * See ISO/IEC 7816-4 Section 5.1.3
         *
         * @return True if SW = 0x62XX or 0x63XX, otherwise false
         */
        get() {
            return if (trailer[0] == 0x62.toByte() || trailer[0] == 0x63.toByte()) {
                true
            } else {
                false
            }
        }

    val isExecutionError: Boolean
        /**
         * Checks if the status bytes indicates an execution error.
         * See ISO/IEC 7816-4 Section 5.1.3
         *
         * @return True if SW = 0x64XX to 0x66XX, otherwise false
         */
        get() {
            return if (trailer[0] == 0x64.toByte() || trailer[0] == 0x65.toByte() || trailer[0] == 0x66.toByte()) {
                true
            } else {
                false
            }
        }

    val isCheckingError: Boolean
        /**
         * Checks if the status bytes indicates an checking error.
         * See ISO/IEC 7816-4 Section 5.1.3
         *
         * @return True if SW = 0x67XX to 0x6FXX, otherwise false
         */
        get() {
            if ((trailer[0].toInt() and 0xF0) == 0x60.toByte().toInt()) {
                for (b in 0x07..0xe) {
                    if ((trailer[0].toInt() and 0x0F) == b.toInt()) {
                        return true
                    }
                }
            }
            return false
        }

    /**
     * Checks if the status bytes equals to a element of the list of positive responses.
     *
     * @param responses Positive responses
     * @return True if the status bytes equals to a element of the list of positive responses, otherwise false
     */
    fun isPositiveResponse(responses: List<ByteArray>): Boolean {
        for (response in responses) {
            if (response.contentEquals(trailer)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns the byte encoded APDU: TRAILER | DATA
     *
     * @return Encoded APDU
     */
    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream(data.size + 2)

        try {
            baos.write(data)
            baos.write(trailer)
        } catch (e: Exception) {
            logger.error("Exception", e)
        }

        return baos.toByteArray()
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    fun toHexString(): String? {
        return ByteUtils.toHexString(toByteArray())
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    override fun toString(): String {
        return ByteUtils.toHexString(toByteArray(), true)!!
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CardResponseAPDU::class.java)

        /**
         * Returns the data field of the APDU.
         *
         * @param responseAPDU Response APDU
         * @return Data field of the APDU
         */
        @JvmStatic
        fun getData(responseAPDU: ByteArray): ByteArray? {
            require(responseAPDU.size >= 2) { "Malformed APDU" }

            return ByteUtils.copy(responseAPDU, 0, responseAPDU.size - 2)
        }

        /**
         * Returns the trailer (status bytes) of the APDU.
         *
         * @param responseAPDU Response APDU
         * @return Trailer of the APDU
         */
        @JvmStatic
        fun getTrailer(responseAPDU: ByteArray): ByteArray? {
            require(responseAPDU.size >= 2) { "Malformed APDU" }

            return ByteUtils.copy(responseAPDU, responseAPDU.size - 2, 2)
        }
    }
}
