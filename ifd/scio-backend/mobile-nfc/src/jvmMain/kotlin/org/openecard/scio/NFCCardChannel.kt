/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOException
import java.io.IOException
import java.nio.ByteBuffer

private val LOG = KotlinLogging.logger {  }

/**
 * NFC implementation of smartcardio's cardChannel interface.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class NFCCardChannel(override val card: AbstractNFCCard) : SCIOChannel {
    @Throws(SCIOException::class)
    override fun close() {
		LOG.debug { "Channels for NFC cards do not close on demand." }
    }

    override val channelNumber: Int
        get() = 0

    @Throws(SCIOException::class, IllegalStateException::class)
    override fun transmit(apdu: CardCommandAPDU): CardResponseAPDU {
        return transmit(apdu.toByteArray())
    }

    @Throws(SCIOException::class, IllegalStateException::class)
    override fun transmit(apdu: ByteArray): CardResponseAPDU {
        synchronized(card) {
            try {
                return CardResponseAPDU(card.transceive(apdu))
            } catch (ex: IOException) {
                check(card.isTagPresent) { "Transmit of apdu command failed, because the card has been removed." }

                // TODO: check if the error code can be chosen more specifically
                throw SCIOException("Transmit failed.", SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, ex)
            }
        }
    }

    @Throws(SCIOException::class, IllegalStateException::class)
    override fun transmit(command: ByteBuffer, response: ByteBuffer): Int {
        val cra = transmit(command.array())
        val data = cra.toByteArray()
        response.put(data)

        return data.size
    }

    override val isBasicChannel: Boolean = true

    override val isLogicalChannel: Boolean = false

}
