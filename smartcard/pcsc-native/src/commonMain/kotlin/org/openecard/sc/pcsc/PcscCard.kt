package org.openecard.sc.pcsc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.isNormalProcessed
import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardCapabilities
import org.openecard.sc.iface.CardProtocol
import org.openecard.sc.iface.toAtr

private val log = KotlinLogging.logger { }

class PcscCard(
	private val card: au.id.micolous.kotlin.pcsc.Card,
	override val terminalConnection: PcscTerminalConnection,
) : Card {
	override val protocol: CardProtocol by lazy {
		if (isContactless) {
			CardProtocol.TCL
		} else {
			card.protocol.toSc()
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	private val atrValue by lazy {
		log.debug { "calling PCSC [$this] Card.status()" }
		card
			.status()
			.atr
			.toUByteArray()
			.toAtr()
	}

	override val isContactless: Boolean by lazy {
		try {
			val getUidCmd = CommandApdu(0xFF.toUByte(), 0xCA.toUByte(), 0x00.toUByte(), 0x00.toUByte(), le = 0xFF.toUShort())
			val response = basicChannel.transmit(getUidCmd)
			response.isNormalProcessed
		} catch (ex: Exception) {
			// don't care
			false
		}
	}

	override val basicChannel: PcscCardChannel by lazy {
		PcscCardChannel(card, this, 0)
	}
	override var setCapabilities: CardCapabilities? = null

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun atr(): Atr = mapScioError { atrValue }

	override fun openLogicalChannel(): PcscCardChannel {
		TODO("Not yet implemented")
	}

	override fun toString(): String = "PcscCard($terminalConnection)"
}
