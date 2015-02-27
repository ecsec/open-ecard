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

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.common.CardCommandAPDU;


/**
 * Represents a channel to a smart card.
 *
 * @author Wael Alkhatib
 */
public interface SCIOChannel {

    /**
     * Gets the card instance this channel is associated with.
     * Even if the card is already disconnected, this method returns the card object.
     *
     * @return The card object associated with this channel.
     */
    @Nonnull
    SCIOCard getCard();

    /**
     * Gets the channel number of this channel.
     * The basic channel has number 0, all logical channels have postive values.
     * <p>Contrary to the implementation of the Java Smartcard IO, this method does not throw an
     * {@link IllegalStateException} when the card is not connected.</p>
     *
     * @return Number of the channel.
     */
    int getChannelNumber();

    /**
     * Tests if this is a basic channel.
     *
     * @return {@code true} if the channel represented by this instance is a basic channel, {@code} false otherwise.
     */
    boolean isBasicChannel();

    /**
     * Tests if this is a logical channel.
     *
     * @return {@code true} if the channel represented by this instance is a logical channel, {@code} false otherwise.
     */
    boolean isLogicalChannel();

    /**
     * Transmits the given command APDU to the card.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link #close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     *
     * @param command Command APDU, which should be sent to the card.
     * @return The response APDU after the given command APDU is processed.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL} command.
     * @throws NullPointerException Thrown in case the argument is {@code null}.
     */
    @Nonnull
    CardResponseAPDU transmit(@Nonnull byte[] command) throws SCIOException, IllegalStateException;

    /**
     * Transmits the given command APDU to the card.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link #close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     *
     * @param command Command APDU, which should be sent to the card.
     * @return The response APDU after the given command APDU is processed.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL} command.
     * @throws NullPointerException Thrown in case the argument is {@code null}.
     */
    @Nonnull
    CardResponseAPDU transmit(@Nonnull CardCommandAPDU command) throws SCIOException, IllegalStateException;

    /**
     * Transmits the given command APDU to the card.
     * <p>The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.</p>
     * <p>The command APDU is read from the current position in the command buffer and the response is written beginning
     * at the current position of the response buffer. The response buffer's pointer is advanced by the number of bytes
     * written to the buffer. This is equal to the number of bytes read which is returned by this function.</p>
     * <p>Note that this method cannot be used to transmit {@code MANAGE CHANNEL} APDUs. Logical channels should be
     * managed using the {@link SCIOCard#openLogicalChannel()} and {@link #close()} methods.</p>
     * <p>Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
     * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:</p>
     * <ul>
     * <li>if the response APDU has an SW1 of 61, the implementation should issue a {@code GET RESPONSE} command using
     * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
     * exchanges is concatenated to form the final response body.</li>
     * <li>if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.</li>
     * </ul>
     *
     * @param command Buffer containing the command APDU, which should be sent to the card.
     * @param response Buffer where the response APDU will be written to.
     * @return The number of bytes written to the response APDU buffer.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
     * @throws IllegalArgumentException Thrown if the APDU encodes a {@code MANAGE CHANNEL} command, if command and
     *   response are the same object, or when there is not enough space in the response buffer.
     * @throws NullPointerException Thrown in case one of the arguments is {@code null}.
     */
    int transmit(@Nonnull ByteBuffer command, @Nonnull ByteBuffer response) throws SCIOException, IllegalStateException;

    /**
     * Closes this CardChannel.
     * The logical channel is closed by issuing a {@code MANAGE CHANNEL} command that should use the format
     * [xx 70 80 0n] where n is the channel number of this channel and xx is the CLA byte that encodes this logical
     * channel and has all other bits set to 0. After this method returns, calling other methods in this class will
     * raise an IllegalStateException.
     * <p>Note that the basic logical channel cannot be closed using this method. It can be closed by calling
     * {@link SCIOCard#disconnect(boolean)}.
     *
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown if this channel represents a basic channel.
     */
    void close() throws SCIOException;

}
