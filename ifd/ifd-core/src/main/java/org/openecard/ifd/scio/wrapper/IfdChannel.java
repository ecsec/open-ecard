/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.ifd.scio.wrapper;

import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.Protocol;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.ifd.scio.TransmitException;


/**
 *
 * @author Tobias Wich
 */
public interface IfdChannel {

    /**
     * Terminate the command submission thread and close the channel.
     * Note that basic channels are not closed by the {@link SCIOChannel#close()} method.
     *
     * @throws SCIOException Thrown in case the channel could not be closed properly.
     */
    void shutdown() throws SCIOException;

    void reconnect() throws SCIOException;

    /**
     * Gets the channel bound in this instance.
     * Be careful when issuing commands as they might fail due to running transactions.
     *
     * @return The channel bound in this instance.
     */
    @Nonnull
    SCIOChannel getChannel();

    /**
     * Transmits the given command APDU to the card and evaluates the response against the given response codes.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link SCIOChannel#close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     * <p>The expected responses may be empty meaning that any outcome is acceptable. One byte status codes represent
     * wildcards by ignoring the second byte of the actual result code.</p>
     *
     * @param input Command APDU, which should be sent to the card.
     * @param responses Expected response codes. May be empty if any code is acceptable. One byte wildcard codes are
     *   also allowed.
     * @return The response APDU after the given command APDU is processed.
     * @throws TransmitException Thrown in case the result contained unexpected response codes.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL}.
     * @throws NullPointerException Thrown in case the argument is {@code null}.
     * @throws InterruptedException if the user cancels the process
     */
    @Nonnull
    byte[] transmit(@Nonnull byte[] input, @Nonnull List<byte[]> responses) throws TransmitException,
	    SCIOException, IllegalStateException, InterruptedException;

    /**
     * Sends a control command to the terminal.
     *
     * @param controlCode The control code of the command.
     * @param command The command data. The data may be empty.
     * @return The response of the command. Note that this is not necessarily an APDU.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see
     *   {@link SCIOCard#disconnect(boolean)}.
     * @throws NullPointerException Thrown in case the command data is {@code null}.
     * @throws InterruptedException if the user cancels the process
     */
    @Nonnull
    byte[] transmitControlCommand(final int controlCode, final @Nonnull byte[] command) throws SCIOException,
	    IllegalStateException, NullPointerException, InterruptedException;

    /**
     * Starts a transaction on the card.
     * Usually the transaction is bound to the current thread, but an implementation may decide to choose another
     * exclusion mechanism.
     *
     * @throws SCIOException Thrown if exclusive access is already set or it could not be set due to some other problem.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see
     *   {@link SCIOCard#disconnect(boolean)}).
     * @throws InterruptedException if the user cancels the process
     */
    void beginExclusive() throws SCIOException, IllegalStateException, InterruptedException;

    /**
     * Ends the exclusive access to the card.
     * Exclusive access must have been established previously with the {@link #beginExclusive()} method.
     *
     * @throws SCIOException Thrown in case the operation failed due to some unkown reason.
     * @throws IllegalStateException Thrown in case the card is already disconnected (see
     *   {@link SCIOCard#disconnect(boolean)}), or no {@link #beginExclusive()} call has been issued before.
     * @throws InterruptedException if the user cancels the process
     */
    void endExclusive() throws SCIOException, IllegalStateException, InterruptedException;

    boolean isSM();

    void addSecureMessaging(Protocol protocol);

    void removeSecureMessaging();

}
