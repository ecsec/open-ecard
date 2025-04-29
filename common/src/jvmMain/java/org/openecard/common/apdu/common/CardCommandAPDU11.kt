/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import org.openecard.common.WSHelper.WSException
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.interfaces.*
import org.openecard.common.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Implements a command APDU.
 * See ISO/IEC 7816-4 Section 5.1.
 *
 * @author Moritz Horsch
 */
open class CardCommandAPDU : CardAPDU {
    /**
     * Returns the header of the APDU: CLA | INS | P1 | P2
     *
     * @return Header of the APDU
     */
    @JvmField
    val header: ByteArray = ByteArray(4)

    /**
     * Returns the length expected (LE) field
     *
     * @return Length expected (LE) field
     */
    var lE: Int = -1
        private set

    /**
     * Returns the length command (LC) field.
     *
     * @return Length command (LC) field
     */
    var lC: Int = -1
        private set

    /**
     * Creates a new command APDU.
     */
    constructor()

    /**
     * Creates a new command APDU.
     *
     * @param commandAPDU APDU
     */
    constructor(commandAPDU: ByteArray) {
        System.arraycopy(commandAPDU, 0, header, 0, 4)
        setBody(ByteUtils.copy(commandAPDU, 4, commandAPDU.size - 4)!!)
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte) {
        header[0] = cla
        header[1] = ins
        header[2] = p1
        header[3] = p2
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, le: Byte) : this(cla, ins, p1, p2, le.toInt() and 0xFF)

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, le: Short) : this(cla, ins, p1, p2, le.toInt() and 0xFFFF)

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, le: Int) : this(cla, ins, p1, p2) {
        this.lE = le
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray) : this(cla, ins, p1, p2) {
        this.data = data

        setLC(data.size)
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray, le: Byte) : this(
        cla,
        ins,
        p1,
        p2,
        data,
        le.toInt() and 0xFF
    )

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray, le: Short) : this(
        cla,
        ins,
        p1,
        p2,
        data,
        le.toInt() and 0xFFFF
    )

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    constructor(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray, le: Int) : this(cla, ins, p1, p2, data) {
        setLE(le)
    }

    var cLA: Byte
        /**
         * Returns the class byte of the APDU.
         *
         * @return Class byte
         */
        get() = (header[0].toInt() and 0xFF).toByte()
        /**
         * Sets the class byte of the APDU.
         *
         * @param cla Class byte
         */
        set(cla) {
            header[0] = cla
        }

    var iNS: Byte
        /**
         * Returns the instruction byte of the APDU.
         *
         * @return Instruction byte
         */
        get() = (header[1].toInt() and 0xFF).toByte()
        /**
         * Sets the instruction byte of the APDU.
         *
         * @param ins Instruction byte
         */
        set(ins) {
            header[1] = ins
        }

    var p1: Byte
        /**
         * Returns the parameter byte P1 of the APDU.
         *
         * @return Parameter byte P1
         */
        get() = (header[2].toInt() and 0xFF).toByte()
        /**
         * Sets the parameter byte P1 of the APDU.
         *
         * @param p1 Parameter byte P1
         */
        set(p1) {
            header[2] = p1
        }

    var p2: Byte
        /**
         * Returns the parameter byte P2 of the APDU.
         *
         * @return parameter byte P2
         */
        get() = (header[3].toInt() and 0xFF).toByte()
        /**
         * Sets the parameter byte P2 of the APDU.
         *
         * @param p2 Parameter byte P2
         */
        set(p2) {
            header[3] = p2
        }

    var p1P2: ByteArray
        /**
         * Returns the parameter bytes P1 and P2 of the APDU.
         *
         * @return parameter bytes P1 and P2
         */
        get() = byteArrayOf(p1, p2)
        /**
         * Sets the parameter bytes P1 and P2 of the APDU.
         *
         * @param p1p2 Parameter bytes P1 and P2
         */
        protected set(p1p2) {
            p1 = p1p2[0]
            p2 = p1p2[1]
        }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected fun setLC(lc: Byte) {
        setLC(lc.toInt() and 0xFF)
    }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected fun setLC(lc: Short) {
        setLC(lc.toInt() and 0xFFFF)
    }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected fun setLC(lc: Int) {
        require(!(lc < 1 || lc > 65536)) { "Length should be from '1' to '65535'." }
        this.lC = lc
    }

    /**
     * Sets the data field of the APDU. Length command (LC) field will be calculated.
     *
     * @param data Data field
     */
    override fun setData(data: ByteArray) {
        super.setData(data)
        setLC(data.size)
    }

    /**
     * Sets the body (LE, DATA, LC) of the APDU.
     *
     * @param body Body of the APDU
     */
    fun setBody(body: ByteArray) {
        /*
	 * Case 1. : |CLA|INS|P1|P2|
	 * Case 2. : |CLA|INS|P1|P2|LE|
	 * Case 2.1: |CLA|INS|P1|P2|EXTLE|
	 * Case 3. : |CLA|INS|P1|P2|LC|DATA|
	 * Case 3.1: |CLA|INS|P1|P2|EXTLC|DATA|
	 * Case 4. : |CLA|INS|P1|P2|LC|DATA|LE|
	 * Case 4.1: |CLA|INS|P1|P2|EXTLC|DATA|LE|
	 * Case 4.2: |CLA|INS|P1|P2|LC|DATA|EXTLE|
	 * Case 4.3: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
	 */
        try {
            val bais = ByteArrayInputStream(body)
            val length = bais.available()

            // Cleanup
            lC = -1
            lE = -1
            data = ByteArray(0)

            if (length == 0) {
                // Case 1. : |CLA|INS|P1|P2|
            } else if (length == 1) {
                // Case 2 |CLA|INS|P1|P2|LE|
                lE = (bais.read() and 0xFF)
            } else if (length < 65536) {
                val tmp = bais.read()

                if (tmp == 0) {
                    // Case 2.1, 3.1, 4.1, 4.3
                    if (bais.available() < 3) {
                        // Case 2.1 |CLA|INS|P1|P2|EXTLE|
                        lE = parseExtLeNumber(bais)
                    } else {
                        // Case 3.1, 4.1, 4.3
                        lC = ((bais.read() and 0xFF) shl 8) or (bais.read() and 0xFF)

                        data = ByteArray(lC)
                        bais.read(data)

                        if (bais.available() == 1) {
                            // Case 4.1 |CLA|INS|P1|P2|EXTLC|DATA|LE|
                            lE = (bais.read() and 0xFF)
                        } else if (bais.available() == 2) {
                            // Case 4.3 |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
                            lE = parseExtLeNumber(bais)
                        } else if (bais.available() == 3) {
                            if (bais.read() == 0) {
                                // Case 4.3 |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
                                lE = parseExtLeNumber(bais)
                            } else {
                                throw IllegalArgumentException("Malformed APDU.")
                            }
                        } else require(bais.available() <= 3) { "Malformed APDU." }
                    }
                } else if (tmp > 0) {
                    // Case 3, 4, 4.2
                    lC = (tmp and 0xFF)
                    data = ByteArray(lC)
                    bais.read(data)

                    if (bais.available() == 1) {
                        // Case 4 |CLA|INS|P1|P2|LC|DATA|LE|
                        setLE(bais.read().toByte())
                    } else if (bais.available() == 3) {
                        // Case 4.2 |CLA|INS|P1|P2|LC|DATA|EXTLE|
                        bais.read() // throw away first byte
                        lE = parseExtLeNumber(bais)
                    } else require(!(bais.available() == 2 || bais.available() > 3)) { "Malformed APDU." }
                } else {
                    throw IllegalArgumentException("Malformed APDU.")
                }
            } else {
                throw IllegalArgumentException("Malformed APDU.")
            }
        } catch (e: Exception) {
            LOG.error("Exception", e)
        }
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    fun setLE(le: Byte) {
        if (le == 0x00.toByte()) {
            setLE(256)
        } else {
            setLE(le.toInt() and 0xFF)
        }
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    fun setLE(le: Short) {
        if (le == 0x0000.toShort()) {
            setLE(65536)
        } else {
            setLE(le.toInt() and 0xFFFF)
        }
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    fun setLE(le: Int) {
        require(!(le < 0 || le > 65536)) { "Length should be from '1' to '65535'." }
        this.lE = le
    }

    /**
     * Updates the class byte of the header to indicate command chaining.
     * See ISO/IEC 7816-4 Section 5.1.1.1
     */
    fun setChaining() {
        header[0] = (header[0].toInt() or 0x10).toByte()
    }

    val chainingIterator: Iterable<*>
        /**
         * Returns a iterator over the chaining APDUs.
         *
         * @return Iterator containing the APDUs.
         */
        get() {
            // TODO: Implement the chaining iterator and add generic type to Iterable. I suppose it is this class.
            throw IllegalAccessError("Not implemented yet")
        }

    /**
     * Updates the class byte of the header to indicate Secure Messaging.
     * See ISO/IEC 7816-4 Section 6
     */
    fun setSecureMessaging() {
        header[0] = (header[0].toInt() or 0x0C).toByte()
    }

    val isSecureMessaging: Boolean
        /**
         * Return true if the header of the APDU indicates Secure Messaging.
         *
         * @return True if APDU is a Secure Messaging APDU.
         */
        get() = ((header[0].toInt() and 0x0F.toByte().toInt()) == 0x0C.toByte().toInt())

    /**
     * Returns the encoded APDU: CLA | INS | P1 | P2 | (EXT)LC | DATA | (EXT)LE
     *
     * @return Encoded APDU
     */
    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()

        try {
            // Write APDU header
            baos.write(header)
            // Write APDU LC field.
            encodeLcField(baos)
            // Write APDU data field
            baos.write(data)
            // Write APDU LE field.
            encodeLeField(baos)
        } catch (ex: IOException) {
            LOG.error("Failed to create APDU in memory.", ex)
        }

        return baos.toByteArray()
    }

    private fun encodeLcField(baos: ByteArrayOutputStream) {
        if (lC > 255 || (lE > 256 && lC > 0)) {
            // Encoded extended LC field in three bytes.
            baos.write(CardAPDU.Companion.x00.toInt())
            baos.write((lC shr 8).toByte().toInt())
            baos.write(lC.toByte().toInt())
        } else if (lC > 0) {
            // Write short LC field
            baos.write(lC.toByte().toInt())
        }
    }

    fun encodeLcField(): ByteArray {
        val baos = ByteArrayOutputStream()
        encodeLcField(baos)
        return baos.toByteArray()
    }

    @Throws(IOException::class)
    private fun parseExtLeNumber(`in`: InputStream): Int {
        val high = `in`.read()
        val low = `in`.read()
        val num = ((high and 0xFF) shl 8) or (low and 0xFF)
        return if (num == 0) {
            65536
        } else {
            num
        }
    }

    private fun encodeLeField(baos: ByteArrayOutputStream) {
        if (lE > 256) {
            // Write extended LE field.
            if (lC == 0 || lC == -1) {
                // Encoded extended LE field in three bytes.
                baos.write(CardAPDU.Companion.x00.toInt())
            }
            // Encoded extended LE field in two bytes if extended LC field is present.
            // If more bytes are requested than possible, assume the maximum.
            if (lE >= 65536) {
                baos.write(CardAPDU.Companion.x00.toInt())
                baos.write(CardAPDU.Companion.x00.toInt())
            } else {
                baos.write((lE shr 8).toByte().toInt())
                baos.write(lE.toByte().toInt())
            }
        } else if (lE > 0) {
            if (lC > 255) {
                // Write extended LE field in two bytes because extended LC field is present.
                baos.write((lE shr 8).toByte().toInt())
                baos.write(lE.toByte().toInt())
            } else {
                // Write short LE field
                baos.write(lE.toByte().toInt())
            }
        }
    }

    fun encodeLeField(): ByteArray {
        val baos = ByteArrayOutputStream()
        encodeLeField(baos)
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

    /**
     * Creates a new Transmit message.
     *
     * @param slotHandle Slot handle
     * @return Transmit
     */
    open fun makeTransmit(slotHandle: ByteArray?): Transmit {
        return makeTransmit(slotHandle, ArrayList(0))
    }

    /**
     * Creates a new Transmit message.
     *
     * @param slotHandle Slot handle
     * @param responses Positive responses
     * @return Transmit
     */
    fun makeTransmit(slotHandle: ByteArray?, responses: List<ByteArray?>): Transmit {
        val apdu = InputAPDUInfoType()
        apdu.inputAPDU = toByteArray()
        apdu.acceptableStatusCode.addAll(responses)

        val t = Transmit()
        t.slotHandle = slotHandle
        t.inputAPDUInfo.add(apdu)

        return t
    }

    /**
     * Transmit the APDU.
     * This function uses 0x9000 as expected status code and raises an error if a different result is returned by the
     * card.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @return Response APDU
     * @throws APDUException
     */
    @Throws(APDUException::class)
    fun transmit(dispatcher: Dispatcher, slotHandle: ByteArray?): CardResponseAPDU {
        return transmit(dispatcher, slotHandle, CardCommandStatus.responseOk())
    }

    /**
     * Transmit the APDU.
     *
     * @param dispatcher Dispatcher
     * @param slotHandle Slot handle
     * @param responses List of positive responses
     * @return Response APDU
     * @throws APDUException
     */
    @Throws(APDUException::class)
    fun transmit(dispatcher: Dispatcher, slotHandle: ByteArray?, responses: List<ByteArray?>?): CardResponseAPDU {
        val t: Transmit
        var tr: TransmitResponse? = null

        try {
            t = if (responses != null) {
                makeTransmit(slotHandle, responses)
            } else {
                makeTransmit(slotHandle)
            }

            tr = dispatcher.safeDeliver(t) as TransmitResponse
            checkResult(tr)
            val responseAPDU = CardResponseAPDU(tr)

            return responseAPDU
        } catch (ex: WSException) {
            throw APDUException(ex, tr!!)
        } catch (ex: Exception) {
            throw APDUException(ex)
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(CardCommandAPDU::class.java)

        /**
         * Returns the header of the APDU.
         *
         * @param commandAPDU Command APDU
         * @return Header of the APDU
         */
        @JvmStatic
        fun getHeader(commandAPDU: ByteArray): ByteArray? {
            require(commandAPDU.size >= 4) { "Malformed APDU" }

            return ByteUtils.copy(commandAPDU, 0, 4)
        }

        /**
         * Returns the body of the APDU (LC*|DATA|LE*).
         *
         * @param commandAPDU Command APDU
         * @return Body of the APDU
         */
        @JvmStatic
        fun getBody(commandAPDU: ByteArray): ByteArray? {
            require(commandAPDU.size >= 4) { "Malformed APDU" }

            return ByteUtils.copy(commandAPDU, 4, commandAPDU.size - 4)
        }
    }
}
