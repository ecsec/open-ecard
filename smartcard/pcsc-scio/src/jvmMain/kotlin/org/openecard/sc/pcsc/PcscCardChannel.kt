package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.AbstractCardChannel
import javax.smartcardio.CommandAPDU

private val log = KotlinLogging.logger { }

class PcscCardChannel internal constructor(
	override val card: PcscCard,
	internal val channel: javax.smartcardio.CardChannel,
) : AbstractCardChannel() {
	override val channelNumber: Int by lazy {
		channel.channelNumber
	}

	@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
	override fun transmitRaw(apdu: CommandApdu): ResponseApdu =
		mapScioError {
			log.debug { "Sending APDU: $apdu" }
			val response = channel.transmit(CommandAPDU(apdu.toBytes.toByteArray()))
			log.debug { "Received APDU: ${response.bytes.toResponseApdu()}" }
			response.bytes.toResponseApdu()
		}

	override fun close() =
		mapScioError {
			if (isLogicalChannel) {
				channel.close()
			}
		}
}
