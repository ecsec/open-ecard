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
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.toAtr
import kotlin.UByteArray

private val logger = KotlinLogging.logger { }

class AndroidNfcCard(
	override val terminalConnection: AndroidTerminalConnection,
) : Card {
	val tag
		get() = terminalConnection.tag

	@OptIn(ExperimentalUnsignedTypes::class)
	override val atr: Atr
		get() {
			val histBytesTmp = tag?.historicalBytes ?: tag?.hiLayerResponse
			// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
			return if (histBytesTmp == null) {
				UByteArray(0).toAtr()
			} else {
				mutableListOf<Int>()
					.apply {
						// Initial Header
						add(0x3B)
						// T0
						add((0x80 or (histBytesTmp.size and 0xF)))
						// TD1
						add(0x80)
						// TD2
						add(0x01)
						// ISO14443A: The historical bytes from ATS response.
						// ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
						// TODO: check that the HiLayerResponse matches the requirements for ISO14443B
						addAll(histBytesTmp.map { it.toInt() })

						// TCK: Exclusive-OR of bytes T0 to Tk
						var chkSum = 0
						for (i in 1..<size) {
							chkSum = chkSum xor this[i]
						}
						add(chkSum)
					}.map { it.toUByte() }
					.toUByteArray()
					.toAtr()
			}
		}

	override val protocol = CardProtocol.TCL
	override val isContactless = true

	override val basicChannel = AndroidCardChannel(this)

	override var capabilities: CardCapabilities?
		get() = TODO("Not yet implemented")
		set(value) {}

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
		when (val tag = card.tag) {
			null -> throw RemovedCard()
			else ->
				mapScioError {
					tag.transceive(apdu.toBytes.toByteArray()).toResponseApdu()
				}
		}

	// only relevant for logic channels
	override fun close() {
		TODO("Not yet implemented")
	}
}
