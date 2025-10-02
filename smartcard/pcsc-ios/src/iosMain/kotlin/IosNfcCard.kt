import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.ResponseApdu
import org.openecard.sc.iface.AbstractCardChannel
import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardCapabilities
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.CardProtocol
import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.RemovedCard
import kotlin.UByteArray

private val logger = KotlinLogging.logger { }

class IosNfcCard(
	override val terminalConnection: IosTerminalConnection,
) : Card {
	internal val tag
		get() = terminalConnection.tag ?: throw RemovedCard()

	override fun atr(): Atr {
		logger.debug { "Historical bytes from card: ${tag.historicalBytes}" }
		val hist = tag.historicalBytes?.toUByteArray() ?: ubyteArrayOf()
		terminalConnection.terminal.currentSession?.setAlertMessage(IosNfcAlertMessages.cardConnectedMessage)
		return Atr.fromHistoricalBytes(hist)
	}

	override val protocol = CardProtocol.TCL
	override val isContactless = true

	override val basicChannel = IosCardChannel(this)

	override var setCapabilities: CardCapabilities? = null

	override fun getCapabilities(): CardCapabilities? = atr().historicalBytes?.cardCapabilities ?: setCapabilities

	override fun openLogicalChannel(): CardChannel {
		TODO("Not yet implemented")
	}
}

class IosCardChannel internal constructor(
	override val card: IosNfcCard,
	override val channelNumber: Int = 0,
) : AbstractCardChannel() {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun transmitRaw(apdu: CommandApdu): ResponseApdu {
		val iosApdu = apdu.toIosApdu()

		val res = CompletableDeferred<ResponseApdu>()
		card.tag.sendCommandAPDU(
			iosApdu,
		) { data, sw1, sw2, error ->
			logger.debug {
				"Response (sw1: ${sw1.toHexString()}, sw2: ${sw2.toHexString()}, " +
					"Error: $error Data:\n" +
					"${data?.toUByteArray()?.toHexString()})"
			}
			when (error?.code) {
				null -> {
					res.complete(
						ResponseApdu(data?.toUByteArray() ?: UByteArray(0), sw1, sw2),
					)
				}
				100L,
				102L,
				-> {
					logger.debug { "Converting error to removed card." }
					res.cancel(
						cause =
							CancellationException(cause = RemovedCard()),
					)
				}
				else -> {
					logger.debug { "Converting error to comm error due to unknown code." }
					res.cancel(
						cause =
							CancellationException(cause = CommError(msg = error.localizedDescription)),
					)
				}
			}
		}
		return try {
			runBlocking {
				res.await()
			}
		} catch (e: CancellationException) {
			when (val c = e.cause) {
				null -> throw CommError()
				else -> throw c
			}
		}
	}

	// only relevant for logic channels
	override fun close() {
		TODO("Not yet implemented")
	}
}
