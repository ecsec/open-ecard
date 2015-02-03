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
 * Provides an interface for SCIO terminals.
 *
 * @author Wael Alkhatib
 */
public interface SCIOTerminal {

    /**
     * Returns the name of this terminal.
     * The name is unique amoung all terminals managed by the respective {@link SCIOTerminals} instance.
     *
     * @return The name of the terminal.
     */
    @Nonnull
    String getName();

    /**
     * Connects the card inserted in this terminal.
     * If a connection has previously established using the specified protocol, this method returns the same Card object
     * as the previous call.
     *
     * @param protocol The protocol to use.
     * @return Connected card instance.
     * @throws SCIOException Thrown if a connection could not be established using the specified protocol or if a
     *   connection has previously been established using a different protocol.
     * @throws IllegalStateException Thrown if the terminal or the card is not available anymore.
     * @throws NullPointerException Thrown if the given protocol is {@code null}.
     * @throws SecurityException Thrown in case this operation is not allowed.
     */
    @Nonnull
    SCIOCard connect(@Nonnull SCIOProtocol protocol) throws SCIOException, IllegalStateException;

    /**
     * Gets whether a card is present in this terminal or not.
     *
     * @return {@code true} if a card is present in this terminal, {@code false} otherwise.
     * @throws SCIOException Thrown in case the status could not be determined.
     */
    boolean isCardPresent() throws SCIOException;

    /**
     * Waits until a card is present in the terminal.
     * The call blocks either until a card is present, or timeout is reached. If a card is already present, this
     * function returns immediately.
     *
     * @param timeout Timeout in milliseconds. 0 indicates an infinite timeout. Negative values are forbidden.
     * @return {@code true} When the function returned due to a card present event, {@code false} when timeout has been
     *   reached.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalArgumentException Thrown in case timeout was negative.
     */
    boolean waitForCardPresent(long timeout) throws SCIOException;

    /**
     * Waits until a card is removed from the terminal.
     * The call blocks either until the card is removed, or timeout is reached. If no card is present, this function
     * returns immediately.
     *
     * @param timeout Timeout in milliseconds. 0 indicates an infinite timeout. Negative values are forbidden.
     * @return {@code true} When the function returned due to a card removed event, {@code false} when timeout has been
     *   reached.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalArgumentException Thrown in case timeout was negative.
     */
    boolean waitForCardAbsent(long timeout) throws SCIOException;

}
