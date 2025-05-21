package org.openecard.sc.pcsc

import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CardProtocol
import org.openecard.sc.iface.CommandApdu
import org.openecard.sc.iface.isNormalProcessed
import org.openecard.sc.iface.toAtr
import org.openecard.sc.iface.transmit
import javax.smartcardio.ATR

class PcscCard(
	override val terminalConnection: PcscTerminalConnection,
	internal val scioCard: javax.smartcardio.Card,
) : Card {
	override val atr: Atr by lazy {
		scioCard.atr.toAtr()
	}
	override val protocol: CardProtocol by lazy {
		scioCard.protocol.toCardProtocol(isContactless)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
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
	override val basicChannel: CardChannel by lazy {
		PcscCardChannel(this, scioCard.basicChannel)
	}

	override fun openLogicalChannel(): CardChannel =
		mapScioError {
			PcscCardChannel(this, scioCard.openLogicalChannel())
		}
}

@OptIn(ExperimentalUnsignedTypes::class)
internal fun ATR.toAtr(): Atr = this.bytes.toUByteArray().toAtr()

internal fun String.toCardProtocol(isContactless: Boolean): CardProtocol =
	if (isContactless) {
		CardProtocol.TCL
	} else {
		when (this) {
			"T=0" -> CardProtocol.T0
			"T=1" -> CardProtocol.T1
			"DIRECT" -> CardProtocol.RAW
			else -> throw IllegalArgumentException("Invalid protocol value received from PCSC backend")
		}
	}
