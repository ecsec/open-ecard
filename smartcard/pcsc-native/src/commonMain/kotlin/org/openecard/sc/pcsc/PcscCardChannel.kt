package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.AbstractCardChannel
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.SecureMessaging

private val log = KotlinLogging.logger { }

class PcscCardChannel(
	private val hwCard: au.id.micolous.kotlin.pcsc.Card,
	override val card: Card,
	override val channelNumber: Int,
) : AbstractCardChannel() {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun transmitRaw(apdu: CommandApdu): ResponseApdu =
		mapScioError {
			val maxBufSize = au.id.micolous.kotlin.pcsc.Card.MAX_BUFFER_SIZE_EXTENDED
			log.trace { "calling PCSC [$this] Card.transmit(apdu=...)" }
			val respData = hwCard.transmit(apdu.toBytes.toByteArray(), maxBufSize)
			return respData.toResponseApdu()
		}

	override fun close() =
		mapScioError {
			when (channelNumber) {
				// basic channel does not need to be closed
				0 -> {}

				else -> {
					TODO("Not yet implemented")
				}
			}
		}

	override fun toString(): String = "PcscCardChannel(num=$channelNumber, card=$card)"
}
