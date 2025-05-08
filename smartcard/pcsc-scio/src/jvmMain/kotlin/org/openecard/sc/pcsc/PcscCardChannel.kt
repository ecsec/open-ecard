package org.openecard.sc.pcsc

import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.SecureMessaging
import javax.smartcardio.CommandAPDU

class PcscCardChannel internal constructor(
	override val card: PcscCard,
	internal val channel: javax.smartcardio.CardChannel,
) : CardChannel {
	override val channelNumber: Int by lazy {
		channel.channelNumber
	}

	private val smHandler: MutableList<SecureMessaging> = mutableListOf()

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun transmit(apdu: UByteArray): UByteArray =
		mapScioError {
			val input = smHandler.foldRight(apdu) { sm, last -> sm.processRequest(last) }
			var response = channel.transmit(CommandAPDU(input.toByteArray())).bytes
			smHandler.fold(response.toUByteArray()) { last, sm -> sm.processResponse(last) }
		}

	override fun close() =
		mapScioError {
			if (isLogicalChannel) {
				channel.close()
			}
		}

	override fun pushSecureMessaging(sm: SecureMessaging) {
		smHandler.add(sm)
	}

	override fun popSecureMessaging() {
		smHandler.removeLastOrNull()
	}

	override fun cleanSecureMessaging() {
		smHandler.clear()
	}
}
