package org.openecard.sc.pcsc

import android.nfc.tech.IsoDep
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.SharingViolation
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

class AndroidTerminalConnection(
	override val terminal: AndroidTerminal,
) : TerminalConnection {
	val tag: IsoDep?
		get() = terminal.tag

	override var card = AndroidNfcCard(this)

	override fun isCardConnected() = tag?.isConnected == true

	override fun disconnect(disposition: CardDisposition) {
		when (disposition) {
			CardDisposition.LEAVE -> {}
			CardDisposition.RESET,
			CardDisposition.POWER_OFF,
			CardDisposition.EJECT,
			-> {
				logger.debug { "Note: $disposition is not supported on android. Using ${CardDisposition.LEAVE}. " }
			}
		}
		tag?.close()
	}

	fun connectTag() {
		when (val localTag = tag) {
			null -> throw RemovedCard()
			else -> {
				if (localTag.isConnected) {
					throw SharingViolation()
				} else {
					mapScioError {
						localTag.timeout = 5.seconds.inWholeMilliseconds.toInt()
						localTag.connect()
					}
				}
			}
		}
	}

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) = logger.debug { "Note: reconnect is NOP on android." }

	override fun getFeatures() = emptySet<Feature>()

	override fun beginTransaction() = logger.debug { "Note: beginTransaction is NOP on android." }

	override fun endTransaction() = logger.debug { "Note: endTransaction is NOP on android." }
}
