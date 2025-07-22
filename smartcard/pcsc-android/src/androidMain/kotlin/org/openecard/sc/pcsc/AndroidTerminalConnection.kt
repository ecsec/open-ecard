package org.openecard.sc.pcsc

import android.nfc.tech.IsoDep
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.SharingViolation
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature
import kotlin.time.Duration.Companion.seconds

class AndroidTerminalConnection(
	override val terminal: AndroidTerminal,
) : TerminalConnection {
	val tag: IsoDep?
		get() = terminal.tag

	override val card = AndroidNfcCard(this)

	override val isCardConnected
		get() = tag?.isConnected == true

	override fun disconnect(disposition: CardDisposition) {
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
	) = connectTag()

	override fun getFeatures() = emptySet<Feature>()

	override fun beginTransaction() = Unit

	override fun endTransaction() = Unit
}
