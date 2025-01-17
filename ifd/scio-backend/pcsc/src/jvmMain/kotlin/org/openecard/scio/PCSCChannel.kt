/****************************************************************************
 * Copyright (C) 2014-2018 TU Darmstadt.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.ifd.scio.SCIOChannel
import org.openecard.common.ifd.scio.SCIOException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import javax.annotation.Nonnull
import javax.smartcardio.CardChannel
import javax.smartcardio.CardException
import javax.smartcardio.CommandAPDU
import kotlin.Throws

private val LOG = KotlinLogging.logger {  }

/**
 * PC/SC channel implementation of the SCIOChannel.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
class PCSCChannel internal constructor(
	override val card: PCSCCard,
	private val channel: CardChannel
) : SCIOChannel {

	// pretend channel num = 0 in case there is something really fucked up during init of the card
	override val channelNumber: Int = try {
		channel.channelNumber
	} catch (ex: IllegalStateException) {
		// very unlikely event, that the card is removed during the connect phase
		LOG.error { "Card disconnected during connect phase, pretending to be channel 0 regardless of what it is." }
		0
	}

	override val isBasicChannel: Boolean = channelNumber == 0

    override val isLogicalChannel: Boolean = !isBasicChannel

    @Throws(SCIOException::class)
    override fun transmit(command: ByteArray): CardResponseAPDU {
        try {
            val convertCommand = CommandAPDU(command)
            val response = channel.transmit(convertCommand)
            return CardResponseAPDU(response.bytes)
        } catch (ex: CardException) {
            val msg = "Failed to transmit APDU to the card in terminal '%s'."
            throw SCIOException(String.format(msg, card.terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun transmit(apdu: CardCommandAPDU): CardResponseAPDU {
        return transmit(apdu.toByteArray())
    }

    @Throws(SCIOException::class)
    override fun transmit(command: ByteBuffer, response: ByteBuffer): Int {
        try {
            return channel.transmit(command, response)
        } catch (ex: CardException) {
            val msg = "Failed to transmit APDU to the card in terminal '%s'."
            throw SCIOException(String.format(msg, card.terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun close() {
        // only close logical channels
        if (isLogicalChannel) {
            try {
                channel.close()
            } catch (ex: CardException) {
                val msg = "Failed to close channel to card in terminal '%s'."
                throw SCIOException(String.format(msg, card.terminal.name), PCSCExceptionExtractor.getCode(ex), ex)
            }
        }
    }

}
