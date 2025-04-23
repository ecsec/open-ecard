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

import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardResponseAPDU
import java.nio.ByteBuffer

/**
 * Represents a channel to a smart card.
 *
 * @author Wael Alkhatib
 */
interface SCIOChannel {
	val card: SCIOCard

	/**
	 * Gets the channel number of this channel.
	 * The basic channel has number 0, all logical channels have positive values.
	 *
	 * Contrary to the implementation of the Java Smartcard IO, this method does not throw an
	 * [IllegalStateException] when the card is not connected.
	 *
	 * @return Number of the channel.
	 */
	val channelNumber: Int

	/**
	 * Tests if this is a basic channel.
	 *
	 * @return `true` if the channel represented by this instance is a basic channel, `` false otherwise.
	 */
	val isBasicChannel: Boolean

	/**
	 * Tests if this is a logical channel.
	 *
	 * @return `true` if the channel represented by this instance is a logical channel, `` false otherwise.
	 */
	val isLogicalChannel: Boolean

	/**
	 * Transmits the given command APDU to the card.
	 *
	 * The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.
	 *
	 * Note that this method cannot be used to transmit `MANAGE CHANNEL` APDUs. Logical channels should be
	 * managed using the [SCIOCard.openLogicalChannel] and [.close] methods.
	 *
	 * Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
	 * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:
	 *
	 *  * if the response APDU has an SW1 of 61, the implementation should issue a `GET RESPONSE` command using
	 * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
	 * exchanges is concatenated to form the final response body.
	 *  * if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.
	 *
	 *
	 * @param command Command APDU, which should be sent to the card.
	 * @return The response APDU after the given command APDU is processed.
	 * @throws SCIOException Thrown if the operation failed.
	 * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
	 * @throws IllegalArgumentException Thrown if the APDU encodes a `MANAGE CHANNEL` command.
	 */
	@Throws(SCIOException::class, IllegalStateException::class)
	fun transmit(command: ByteArray): CardResponseAPDU

	/**
	 * Transmits the given command APDU to the card.
	 *
	 * The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.
	 *
	 * Note that this method cannot be used to transmit `MANAGE CHANNEL` APDUs. Logical channels should be
	 * managed using the [SCIOCard.openLogicalChannel] and [.close] methods.
	 *
	 * Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
	 * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:
	 *
	 *  * if the response APDU has an SW1 of 61, the implementation should issue a `GET RESPONSE` command using
	 * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
	 * exchanges is concatenated to form the final response body.
	 *  * if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.
	 *
	 *
	 * Note that this function should in general just call the byte array based version
	 * ([.transmit].
	 *
	 * @param command Command APDU, which should be sent to the card.
	 * @return The response APDU after the given command APDU is processed.
	 * @throws SCIOException Thrown if the operation failed.
	 * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
	 * @throws IllegalArgumentException Thrown if the APDU encodes a `MANAGE CHANNEL` command.
	 */
	@Throws(SCIOException::class, IllegalStateException::class)
	fun transmit(command: CardCommandAPDU): CardResponseAPDU

	/**
	 * Transmits the given command APDU to the card.
	 *
	 * The CLA byte of the command APDU is automatically adjusted to match the channel number of this channel.
	 *
	 * The command APDU is read from the current position in the command buffer and the response is written beginning
	 * at the current position of the response buffer. The response buffer's pointer is advanced by the number of bytes
	 * written to the buffer. This is equal to the number of bytes read which is returned by this function.
	 *
	 * Note that this method cannot be used to transmit `MANAGE CHANNEL` APDUs. Logical channels should be
	 * managed using the [SCIOCard.openLogicalChannel] and [.close] methods.
	 *
	 * Implementations must transparently handle artifacts of the transmission protocol. For example, when using the
	 * T=0 protocol, the following processing should occur as described in ISO/IEC 7816-4:
	 *
	 *  * if the response APDU has an SW1 of 61, the implementation should issue a `GET RESPONSE` command using
	 * SW2 as the Lefield. This process is repeated as long as an SW1 of 61 is received. The response body of these
	 * exchanges is concatenated to form the final response body.
	 *  * if the response APDU is 6C XX, the implementation should reissue the command using XX as the Le field.
	 *
	 *
	 * @param command Buffer containing the command APDU, which should be sent to the card.
	 * @param response Buffer where the response APDU will be written to.
	 * @return The number of bytes written to the response APDU buffer.
	 * @throws SCIOException Thrown if the operation failed.
	 * @throws IllegalStateException Thrown if the card is not connected anymore or the channel has been closed.
	 * @throws IllegalArgumentException Thrown if the APDU encodes a `MANAGE CHANNEL` command, if command and
	 * response are the same object, or when there is not enough space in the response buffer.
	 */
	@Throws(SCIOException::class, IllegalStateException::class)
	fun transmit(
		command: ByteBuffer,
		response: ByteBuffer,
	): Int

	/**
	 * Closes this CardChannel.
	 * The logical channel is closed by issuing a `MANAGE CHANNEL` command that should use the format
	 * [xx 70 80 0n] where n is the channel number of this channel and xx is the CLA byte that encodes this logical
	 * channel and has all other bits set to 0. After this method returns, calling other methods in this class will
	 * raise an IllegalStateException.
	 *
	 * Note that the basic logical channel cannot be closed using this method. It can be closed by calling
	 * [SCIOCard.disconnect]. Calling this method on a basic channel does nothing.
	 *
	 * @throws SCIOException Thrown if the operation failed.
	 */
	@Throws(SCIOException::class)
	fun close()
}
