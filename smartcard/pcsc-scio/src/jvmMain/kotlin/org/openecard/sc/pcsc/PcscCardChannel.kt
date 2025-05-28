package org.openecard.sc.pcsc

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.AbstractCardChannel
import javax.smartcardio.CommandAPDU

class PcscCardChannel internal constructor(
	override val card: PcscCard,
	internal val channel: javax.smartcardio.CardChannel,
) : AbstractCardChannel() {
	override val channelNumber: Int by lazy {
		channel.channelNumber
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun transmitRaw(apdu: CommandApdu): ResponseApdu =
		mapScioError {
			val response = channel.transmit(CommandAPDU(apdu.toBytes.toByteArray()))
			response.bytes.toResponseApdu()
		}

	override fun close() =
		mapScioError {
			if (isLogicalChannel) {
				channel.close()
			}
		}
}
