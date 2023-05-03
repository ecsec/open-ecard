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

package org.openecard.common.ifd.scio;

import javax.annotation.Nonnull;


/**
 * Represents a ISO/IEC 7816 smart card.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public interface SCIOCard {

    /**
     * Gets the terminal which is responsible for this card.
     * Even if the terminal is already disconnected, this method returns the terminal object.
     *
     * @return The terminal responsible for this card.
     */
    @Nonnull
    SCIOTerminal getTerminal();

    /**
     * Gets the ATR associated with this card instance.
     *
     * @return The ATR associated with the card.
     */
    @Nonnull
    SCIOATR getATR();

    /**
     * Gets the protocol in use for this card.
     * <p>Note that this method shall normally not return the value {@link SCIOProtocol#ANY}. One exception is when a
     * new protocol, is used which is not yet defined in the enum and {@code ANY} has been used to connect the card.
     *
     * @return Protocol with which the card is connected.
     */
    @Nonnull
    SCIOProtocol getProtocol();

    /**
     * Returns whether the card is connected with a contactless protocol, or not.
     *
     * @return {@code true} if the card is connected with a contactless protocol, {@code false} otherwise.
     */
    boolean isContactless();

    /**
     * Gets the card channel for the basic logical channel.
     * The basic channel has the number 0.
     *
     * @return The basic channel to the card.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see {@link #disconnect(boolean)}.
     */
    @Nonnull
    SCIOChannel getBasicChannel() throws IllegalStateException;

    /**
     * Opens and returns a logical channel to the card.
     * The channel is opened by issuing a MANAGE CHANNEL command. The card must support logical channels, or this
     * method fails.
     *
     * @return The new logical channel.
     * @throws SCIOException Thrown in case the channel could not be opened.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see {@link #disconnect(boolean)}.
     */
    @Nonnull
    SCIOChannel openLogicalChannel() throws SCIOException, IllegalStateException;

    /**
     * Starts a transaction on the card.
     * Usually the transaction is bound to the current thread, but an implementation may decide to choose another
     * exclusion mechanism.
     *
     * @throws SCIOException Thrown if exclusive access is already set or it could not be set due to some other problem.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see {@link #disconnect(boolean)}.
     */
    void beginExclusive() throws SCIOException, IllegalStateException;

    /**
     * Ends the exclusive access to the card.
     * Exclusive access must have been established previously with the {@link #beginExclusive()} method.
     *
     * @throws SCIOException Thrown in case the operation failed due to some unkown reason.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see {@link #disconnect(boolean)},
     *   or no {@link #beginExclusive()} call has been issued before.
     */
    void endExclusive() throws SCIOException, IllegalStateException;

    /**
     * Sends a control command to the terminal.
     *
     * @param controlCode The control code of the command.
     * @param command The command data. The data may be empty.
     * @return The response of the command. Note that this is not necessarily an APDU.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see {@link #disconnect(boolean)}.
     * @throws NullPointerException Thrown in case the command data is {@code null}.
     */
    @Nonnull
    byte[] transmitControlCommand(int controlCode, @Nonnull byte[] command) throws SCIOException,
	    IllegalStateException, NullPointerException;

    /**
     * Disconnects the connection to the card.
     * After the execution of this method, all invocations on methods of {@link SCIOCard} and {@link SCIOChannel}
     * instances which need the connection to this card will yield errors.
     *
     * @param reset If {@code true} the card will be reset during the disconnect.
     * @throws SCIOException Thrown if the operation failed.
     */
    void disconnect(boolean reset) throws SCIOException;

}
