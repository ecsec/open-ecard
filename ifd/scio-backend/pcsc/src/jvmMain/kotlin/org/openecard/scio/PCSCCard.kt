/****************************************************************************
 * Copyright (C) 2014-2015 TU Darmstadt.
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
package org.openecard.scio

import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.ifd.scio.SCIOATR
import org.openecard.common.ifd.scio.SCIOCard
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOProtocol
import org.openecard.common.ifd.scio.SCIOProtocol.Companion.getType
import javax.smartcardio.Card
import javax.smartcardio.CardException
import javax.smartcardio.CommandAPDU

/**
 * PC/SC card implementation of the SCIOCard.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
class PCSCCard internal constructor(override val terminal: PCSCTerminal, private val card: Card) : SCIOCard {

    override val isContactless: Boolean by lazy {
		try {
			val getUidCmd = CardCommandAPDU(0xFF.toByte(), 0xCA.toByte(), 0x00.toByte(), 0x00.toByte(), 0xFF.toShort())
			val convertCommand = CommandAPDU(getUidCmd.toByteArray())
			val response = card.basicChannel.transmit(convertCommand)
			val cr = CardResponseAPDU(response.bytes)
			cr.isNormalProcessed
		} catch (ex: Exception) {
			// don't care
			false
		}
	}

    override val aTR: SCIOATR
        get() {
            val atr = card.atr
            return SCIOATR(atr.bytes)
        }

    override val protocol: SCIOProtocol
        get() {
            val proto = card.protocol
            return getType(proto)
        }


    override val basicChannel: PCSCChannel
        get() = PCSCChannel(this, card.basicChannel)

    @Throws(SCIOException::class)
    override fun openLogicalChannel(): PCSCChannel {
        try {
            return PCSCChannel(this, card.openLogicalChannel())
        } catch (ex: CardException) {
            val msg = "Failed to open logical channel to card in terminal '%s'."
            throw SCIOException(String.format(msg, terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun beginExclusive() {
        try {
            card.beginExclusive()
        } catch (ex: CardException) {
            val msg = "Failed to get exclusive access to the card in terminal '%s'."
            throw SCIOException(String.format(msg, terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun endExclusive() {
        try {
            card.endExclusive()
        } catch (ex: CardException) {
            val msg = "Failed to release exclusive access to the card in terminal '%s'."
            throw SCIOException(String.format(msg, terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun transmitControlCommand(controlCode: Int, command: ByteArray): ByteArray {
        try {
            return card.transmitControlCommand(controlCode, command)
        } catch (ex: CardException) {
            val msg = "Failed to transmit control command to the terminal '%s'."
            throw SCIOException(String.format(msg, terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun disconnect(reset: Boolean) {
        try {
            card.disconnect(reset)
        } catch (ex: CardException) {
            val msg = "Failed to disconnect (reset=%b) the card in terminal '%s'."
            throw SCIOException(String.format(msg, reset, terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }
}
