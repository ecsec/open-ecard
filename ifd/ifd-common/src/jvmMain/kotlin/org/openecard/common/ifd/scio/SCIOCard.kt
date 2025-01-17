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
package org.openecard.common.ifd.scio

/**
 * Represents a ISO/IEC 7816 smart card.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
interface SCIOCard {

    val terminal: SCIOTerminal

    val aTR: SCIOATR

    val protocol: SCIOProtocol

    /**
     * Returns whether the card is connected with a contactless protocol, or not.
     *
     * @return `true` if the card is connected with a contactless protocol, `false` otherwise.
     */
    val isContactless: Boolean

    @get:Throws(IllegalStateException::class)
    val basicChannel: SCIOChannel

    /**
     * Opens and returns a logical channel to the card.
     * The channel is opened by issuing a MANAGE CHANNEL command. The card must support logical channels, or this
     * method fails.
     *
     * @return The new logical channel.
     * @throws SCIOException Thrown in case the channel could not be opened.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see [.disconnect].
     */
    @Throws(SCIOException::class, IllegalStateException::class)
    fun openLogicalChannel(): SCIOChannel

    /**
     * Starts a transaction on the card.
     * Usually the transaction is bound to the current thread, but an implementation may decide to choose another
     * exclusion mechanism.
     *
     * @throws SCIOException Thrown if exclusive access is already set or it could not be set due to some other problem.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see [.disconnect].
     */
    @Throws(SCIOException::class, IllegalStateException::class)
    fun beginExclusive()

    /**
     * Ends the exclusive access to the card.
     * Exclusive access must have been established previously with the [.beginExclusive] method.
     *
     * @throws SCIOException Thrown in case the operation failed due to some unkown reason.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see [.disconnect],
     * or no [.beginExclusive] call has been issued before.
     */
    @Throws(SCIOException::class, IllegalStateException::class)
    fun endExclusive()

    /**
     * Sends a control command to the terminal.
     *
     * @param controlCode The control code of the command.
     * @param command The command data. The data may be empty.
     * @return The response of the command. Note that this is not necessarily an APDU.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see [.disconnect].
     */
    @Throws(SCIOException::class, IllegalStateException::class, NullPointerException::class)
    fun transmitControlCommand(controlCode: Int, command: ByteArray): ByteArray

    /**
     * Disconnects the connection to the card.
     * After the execution of this method, all invocations on methods of [SCIOCard] and [SCIOChannel]
     * instances which need the connection to this card will yield errors.
     *
     * @param reset If `true` the card will be reset during the disconnect.
     * @throws SCIOException Thrown if the operation failed.
     */
    @Throws(SCIOException::class)
    fun disconnect(reset: Boolean)
}
