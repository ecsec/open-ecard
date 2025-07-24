package org.openecard.sc.pcsc
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Parcelable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.ReaderUnsupported
import org.openecard.sc.iface.SmartCardStackMissing
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private val logger = KotlinLogging.logger {}

class AndroidTerminalFactory(
	val androidActivity: Activity,
) : TerminalFactory {
	override val name: String
		get() = "AndroidNFC"

	private var terminals: AndroidTerminals? = null

	val nfcAdapter: NfcAdapter? by lazy {
		(androidActivity.getSystemService(Context.NFC_SERVICE) as NfcManager).defaultAdapter
	}

	val nfcAvailable: Boolean
		get() = nfcAdapter != null

	val nfcEnabled: Boolean
		get() = nfcAvailable && nfcAdapter?.isEnabled == true

	override fun load() =
		when {
			!nfcAvailable ->
				throw SmartCardStackMissing("NFC not available on this device.", null)

			!nfcEnabled ->
				throw ReaderUnavailable("NFC is not enabled in system settings.")

			else ->
				AndroidTerminals(this, androidActivity, nfcAdapter).also {
					terminals = it
				}
		}

	val tagIntentHandler: ((tag: Intent) -> Unit) = {
		terminals?.androidTerminal?.tagIntentHandler(it)
	}
}

class AndroidTerminals internal constructor(
	override val factory: AndroidTerminalFactory,
	val androidActivity: Activity,
	val nfcAdapter: NfcAdapter?,
) : Terminals {
	val androidTerminal: AndroidTerminal by lazy {
		AndroidTerminal(
			this,
			"AndroidNFCTerminal",
			androidActivity,
			nfcAdapter,
		)
	}

	override val supportsControlCommand = false

	override fun establishContext() = Unit

	override val isEstablished = true

	override fun releaseContext() {
		androidTerminal.needNfc = false
		androidTerminal.nfcOff()
	}

	override fun list(): List<Terminal> = listOf(androidTerminal)

	override fun getTerminal(name: String): Terminal? = androidTerminal
}
