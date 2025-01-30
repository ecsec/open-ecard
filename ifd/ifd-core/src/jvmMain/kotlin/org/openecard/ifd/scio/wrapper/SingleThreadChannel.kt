/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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
package org.openecard.ifd.scio.wrapper

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.ifd.Protocol
import org.openecard.common.ifd.RecoverableSecureMessagingException
import org.openecard.common.ifd.scio.*
import org.openecard.common.util.ByteUtils
import org.openecard.ifd.scio.TransmitException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


private val LOG = KotlinLogging.logger { }

/**
 * Implementation of a channel executing all commands in the same thread.
 * Executing commands in the same thread has the effect, that transactions are not broken when the IFD is called from
 * different threads which is the case almost every time.
 *
 * @author Tobias Wich
 */
class SingleThreadChannel : IfdChannel {
	private val exec: ExecutorService
	override var channel: SCIOChannel

	/**
	 * Currently active secure messaging protocol.
	 */
	private var smProtocol: Protocol? = null

	/**
	 * Creates a master instance and launches a command submission thread.
	 * This function connects the terminal with whatever protocol that works.
	 *
	 * @param term Terminal whose channel is to be bound to the thread.
	 * @throws SCIOException Thrown in case the channel could not be established.
	 */
	constructor(term: SCIOTerminal) {
		this.exec = createExecutor()

		val card: SCIOCard = connectCard(term)
		this.channel = card.basicChannel
	}

	/**
	 * Creates a slave instance and launches a command submission thread.
	 *
	 * @param master Master (basic) channel from which the other channel instance is to be derived.
	 * @param isBasic `true` if a basic channel shall be opened, `false` if a logical channel shall be opened.
	 * @throws SCIOException Thrown in case the channel could not be established.
	 */
	constructor(master: SingleThreadChannel, isBasic: Boolean) {
		this.exec = createExecutor()

		val baseCard = master.channel.card
		// connect with protocol that worked for the base card
		val card = baseCard.terminal.connect(baseCard.protocol)
		if (isBasic) {
			this.channel = card.basicChannel
		} else {
			this.channel = card.openLogicalChannel()
		}
	}

	private fun createExecutor(): ExecutorService {
		return Executors.newSingleThreadExecutor(ThreadFactory { r: Runnable? ->
			val num = channel.channelNumber
			val termName = channel.card.terminal.name
			val name = "Channel-${THREAD_NUM.getAndIncrement()} $num '${termName}'"
			val t = Thread(r, name)
			t.setDaemon(true)
			t
		})
	}

	@Throws(SCIOException::class)
	override fun shutdown() {
		exec.shutdown()
		channel.close()
	}

	@Throws(SCIOException::class)
	override fun reconnect() {
		if (channel.isBasicChannel) {
			var card = channel.card
			val term = card.terminal
			channel.close()
			card.disconnect(true)
			card = connectCard(term)

			channel = card.basicChannel
			removeSecureMessaging()
		} else {
			throw RuntimeException("Reconnect called on logical channel.")
		}
	}


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
	 * @throws NullPointerException Thrown in case the argument is `null`.
	 */
	@Throws(SCIOException::class, IllegalStateException::class, InterruptedException::class)
	private fun transmit(command: ByteArray): CardResponseAPDU {
		// send command
		val result = exec.submit(Callable { channel.transmit(command) })
		// return result or evaluate errors
		try {
			return result.get()
		} catch (ex: ExecutionException) {
			// check out the real cause of the error
			val cause = ex.cause
			if (cause is SCIOException) {
				throw cause
			} else if (cause is IllegalStateException) {
				throw cause
			} else if (cause is IllegalArgumentException) {
				throw cause
			} else if (cause is NullPointerException) {
				throw cause
			} else {
				val msg = "Unknown error during APDU submission."
				throw SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause)
			}
		} catch (ex: InterruptedException) {
			result.cancel(true)
			throw InterruptedException("Interruption during transmit.")
		}
	}

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
	 * @throws IllegalArgumentException Thrown if the APDU encodes a `MANAGE CHANNEL`..
	 * @throws NullPointerException Thrown in case the argument is `null`.
	 */
	@Throws(SCIOException::class, IllegalStateException::class, InterruptedException::class)
	private fun transmit(command: CardCommandAPDU): CardResponseAPDU {
		return transmit(command.toByteArray())
	}

	@Throws(TransmitException::class, SCIOException::class, IllegalStateException::class, InterruptedException::class)
	override fun transmit(input: ByteArray, responses: List<ByteArray>): ByteArray {
		var inputAPDU = input
		var result: ByteArray?

		try {
			if (isSM) {
				LOG.debug { "Apply secure messaging to APDU: ${ByteUtils.toHexString(inputAPDU, false)}" }
				inputAPDU = smProtocol!!.applySM(inputAPDU)
			}
			LOG.debug { "Send APDU: ${ByteUtils.toHexString(inputAPDU, false)}" }
			val rapdu = transmit(inputAPDU)
			result = rapdu.toByteArray()
			LOG.debug { "Receive APDU: ${ByteUtils.toHexString(result, false)}" }
			if (isSM) {
				result = smProtocol!!.removeSM(result)
				LOG.debug { "Remove secure messaging from APDU: ${ByteUtils.toHexString(result, false)}" }
			}
		} catch (ex: RecoverableSecureMessagingException) {
			result = ex.errorResponse
		}
		// get status word
		val sw = ByteArray(2)
		sw[0] = result[result.size - 2]
		sw[1] = result[result.size - 1]

		// return without validation when no expected results given
		if (responses.isEmpty()) {
			return result
		}
		// verify result
		for (expected in responses) {
			// one byte codes are used like mask values
			// AcceptableStatusCode-elements containing only one byte match all status codes starting with this byte
			if (ByteUtils.isPrefix(expected, sw)) {
				return result
			}
		}

		// not an expected result
		val msg = "The returned status code is not in the list of expected status codes. The returned code is:\n"
		val tex = TransmitException(result, msg + CardCommandStatus.getMessage(sw))
		throw tex
	}

	@Throws(
		SCIOException::class,
		IllegalStateException::class,
		NullPointerException::class,
		InterruptedException::class
	)
	override fun transmitControlCommand(controlCode: Int, command: ByteArray): ByteArray {
		// send command
		val result = exec.submit(Callable { channel.card.transmitControlCommand(controlCode, command) })
		// return result or evaluate errors
		try {
			return result.get()
		} catch (ex: ExecutionException) {
			// check out the real cause of the error
			val cause = ex.cause
			if (cause is SCIOException) {
				throw cause
			} else if (cause is IllegalStateException) {
				throw cause
			} else if (cause is NullPointerException) {
				throw cause
			} else {
				val msg = "Unknown error during control command submission."
				throw SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause)
			}
		} catch (ex: InterruptedException) {
			result.cancel(true)
			throw InterruptedException("Interruption during transmit control command.")
		}
	}

	@Throws(SCIOException::class, IllegalStateException::class, InterruptedException::class)
	override fun beginExclusive() {
		submitTransaction(true)
	}

	@Throws(SCIOException::class, IllegalStateException::class, InterruptedException::class)
	override fun endExclusive() {
		submitTransaction(false)
	}

	@Throws(SCIOException::class, IllegalStateException::class, InterruptedException::class)
	private fun submitTransaction(start: Boolean) {
		// send command
		val result = exec.submit(Callable {
			val card = channel.card
			if (start) {
				card.beginExclusive()
			} else {
				card.endExclusive()
			}
			null
		})
		// return result or evaluate errors
		try {
			result.get()
		} catch (ex: ExecutionException) {
			// check out the real cause of the error
			val cause = ex.cause
			if (cause is SCIOException) {
				throw cause
			} else if (cause is IllegalStateException) {
				throw cause
			} else {
				val msg = String.format("Unknown error during transaction submission (start=%b).", start)
				throw SCIOException(msg, SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, cause)
			}
		} catch (ex: InterruptedException) {
			result.cancel(true)
			throw InterruptedException("Interruption during transaction submit.")
		}
	}

	override val isSM: Boolean
		get() {
			val result = this.smProtocol != null
			return result
		}

	override fun addSecureMessaging(protocol: Protocol) {
		this.smProtocol = protocol
	}

	override fun removeSecureMessaging() {
		this.smProtocol = null
	}

}

private val THREAD_NUM = AtomicInteger(1)

@Throws(SCIOException::class)
private fun connectCard(term: SCIOTerminal): SCIOCard {
	var card = try {
		term.connect(SCIOProtocol.T1)
	} catch (e1: SCIOException) {
		try {
			term.connect(SCIOProtocol.TCL)
		} catch (e2: SCIOException) {
			try {
				term.connect(SCIOProtocol.T0)
			} catch (e3: SCIOException) {
				try {
					term.connect(SCIOProtocol.ANY)
				} catch (ex: SCIOException) {
					throw SCIOException("Reader refused to connect card with any protocol.", ex.code)
				}
			}
		}
	}
	LOG.info { "Card connected with protocol ${card.protocol}." }
	return card
}
