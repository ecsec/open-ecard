package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.toResponseApdu
import org.openecard.sc.iface.AbstractCardChannel
import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardCapabilities
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CardProtocol
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.RemovedCard

private val logger = KotlinLogging.logger { }

class AndroidNfcCard(
	override val terminalConnection: AndroidTerminalConnection,
) : Card {
	val tag
		get() = terminalConnection.tag ?: throw RemovedCard()

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun atr(): Atr {
		val histBytesTmp = tag.historicalBytes ?: tag.hiLayerResponse
		return histBytesTmp?.let {
			Atr.fromHistoricalBytes(histBytesTmp.toUByteArray())
		} ?: throw CommError("Unsupported card or no valid historical bytes could be read.")
	}

	override val protocol = CardProtocol.TCL
	override val isContactless = true

	override val basicChannel = AndroidCardChannel(this)

	override var setCapabilities: CardCapabilities? = null

	override fun openLogicalChannel(): CardChannel {
		TODO("Not yet implemented")
	}
}

class AndroidCardChannel internal constructor(
	override val card: AndroidNfcCard,
	override val channelNumber: Int = 0,
) : AbstractCardChannel() {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun transmitRaw(apdu: CommandApdu) =
		mapScioError {
			card.tag.transceive(apdu.toBytes.toByteArray()).toResponseApdu()
		}

	// only relevant for logic channels
	override fun close() {
		if (isLogicalChannel) {
			TODO("Not yet implemented")
		}
	}
}
