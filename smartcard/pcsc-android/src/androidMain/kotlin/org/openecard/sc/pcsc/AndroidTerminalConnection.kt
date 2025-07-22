package org.openecard.sc.pcsc

import android.nfc.tech.IsoDep
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature
import kotlin.time.Duration.Companion.seconds

class AndroidTerminalConnection(
	override val terminal: AndroidTerminal,
	private val connectDirectly: Boolean = true,
) : TerminalConnection {
	val tag: IsoDep?
		get() = terminal.tag

	init {
		if (connectDirectly && !isCardConnected) {
			tag?.timeout = 5.seconds.inWholeMilliseconds.toInt()
			tag?.connect()
		}
	}

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

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) {
		if (!isCardConnected) {
			tag?.timeout = 5.seconds.inWholeMilliseconds.toInt()
			tag?.connect()
		}
	}

	override fun getFeatures() = emptySet<Feature>()

	override fun beginTransaction() = Unit

	override fun endTransaction() = Unit
}
