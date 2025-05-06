package org.openecard.sc.pcsc

import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.SecureMessaging
import javax.smartcardio.CommandAPDU

class PcscCardChannel internal constructor(
	override val card: PcscCard,
	internal val channel: javax.smartcardio.CardChannel,
) : CardChannel {
	override val channelNumber: Int
		get() = channel.channelNumber

	private val smHandler: MutableList<SecureMessaging> = mutableListOf()

	override fun transmit(apdu: ByteArray): ByteArray {
		val input = smHandler.foldRight(apdu) { sm, last -> sm.processRequest(last) }
		var response = channel.transmit(CommandAPDU(input)).bytes
		return smHandler.fold(response) { last, sm -> sm.processResponse(last) }
	}

	override fun close() {
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
