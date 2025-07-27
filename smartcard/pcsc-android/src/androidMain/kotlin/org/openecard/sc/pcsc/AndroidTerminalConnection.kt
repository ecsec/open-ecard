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

	override var card: AndroidNfcCard? = null

	override val isCardConnected
		get() = tag?.isConnected == true

	// todo was tun wir genau - leave normally
	override fun disconnect(disposition: CardDisposition) {
		tag?.close()
		card = null
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
						card = AndroidNfcCard(this)
					}
				}
			}
		}
	}

	// todo this is wrongly interpreted - it is for sharemode change n stuff
	// nop in android
	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) = connectTag()

	override fun getFeatures() = emptySet<Feature>()

	override fun beginTransaction() = logger.debug { "Note: beginTransaction is NOP on android" }

	override fun endTransaction() = logger.debug { "Note: endTransaction is NOP on android" }
}
