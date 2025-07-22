package org.openecard.sc.pcsc

import android.nfc.tech.IsoDep
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
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
		get() =
			try {
				tag?.isConnected == true
			} catch (e: SecurityException) {
				terminal.tag = null
				false
			}

	override fun disconnect(disposition: CardDisposition) {
		tag?.close()
	}

	fun connectTag() {
		if (!isCardConnected) {
			mapScioError {
				tag?.timeout = 5.seconds.inWholeMilliseconds.toInt()
				tag?.connect()
			}
		} else {
			throw SharingViolation()
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
