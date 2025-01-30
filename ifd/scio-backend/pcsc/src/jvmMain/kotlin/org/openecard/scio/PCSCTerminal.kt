/****************************************************************************
 * Copyright (C) 2014-2016 TU Darmstadt.
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

import org.openecard.common.ifd.scio.*
import javax.smartcardio.CardException
import javax.smartcardio.CardNotPresentException
import javax.smartcardio.CardTerminal
import kotlin.Throws

/**
 * PC/SC terminal implementation of the SCIOTerminal.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
class PCSCTerminal internal constructor(private val terminal: CardTerminal) : SCIOTerminal {

    override val name: String = terminal.name


    // method is synchronized only to prevent JVM crashes on linux which happens after nPA authentication
    // C  [libpthread.so.0+0xb513]  __pthread_mutex_unlock_usercnt+0x3
    // j  sun.security.smartcardio.PCSC.SCardStatus(J[B)[B+0
    // j  sun.security.smartcardio.CardImpl.isValid()Z+19
    // j  sun.security.smartcardio.TerminalImpl.connect(Ljava/lang/String;)Ljavax/smartcardio/Card;+36
    // j  org.openecard.scio.PCSCTerminal.connect(Lorg/openecard/common/ifd/scio/SCIOProtocol;)Lorg/openecard/common/ifd/scio/SCIOCard;+8
    @Throws(SCIOException::class, IllegalStateException::class)
    override fun connect(protocol: SCIOProtocol): SCIOCard {
        synchronized(CardTerminal::class.java) {
            try {
                val c = terminal.connect(protocol.identifier)
                return PCSCCard(this, c)
            } catch (ex: CardNotPresentException) {
                val msg = "Card has been removed before connect could be finished for terminal '%s'."
                throw SCIOException(String.format(msg, name), SCIOErrorCode.SCARD_W_REMOVED_CARD)
            } catch (ex: CardException) {
                val msg = "Failed to connect the card in terminal '%s'."
                throw SCIOException(String.format(msg, name), PCSCExceptionExtractor.getCode(ex), ex)
            } catch (ex: IllegalArgumentException) {
                val msg = String.format("Protocol %s is not accepted by PCSC stack.", protocol.identifier)
                throw SCIOException(msg, SCIOErrorCode.SCARD_E_PROTO_MISMATCH, ex)
            }
        }
    }

    @get:Throws(SCIOException::class)
	override val isCardPresent: Boolean
        get() {
            try {
                return terminal.isCardPresent
            } catch (ex: CardException) {
                throw SCIOException(
                    "Failed to determine whether card is present or not.",
                    PCSCExceptionExtractor.getCode(ex),
                    ex
                )
            }
        }

    @Throws(SCIOException::class)
    override fun waitForCardPresent(timeout: Long): Boolean {
        try {
            return terminal.waitForCardPresent(timeout)
        } catch (ex: CardException) {
            val msg = "Failed to wait for card present event in terminal '%s'."
            throw SCIOException(String.format(msg, name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }

    @Throws(SCIOException::class)
    override fun waitForCardAbsent(timeout: Long): Boolean {
        try {
            return terminal.waitForCardAbsent(timeout)
        } catch (ex: CardException) {
            val msg = "Failed to wait for card absent event in terminal '%s'."
            throw SCIOException(String.format(msg, name), PCSCExceptionExtractor.getCode(ex), ex)
        }
    }
}
