package org.openecard.sc.pcsc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalStateType
import org.openecard.sc.iface.Terminals
import kotlin.jvm.java

private val logger = KotlinLogging.logger { }

class AndroidTerminal(
	override val terminals: Terminals,
	override val name: String,
	val androidActivity: Activity,
	val nfcAdapter: NfcAdapter?,
) : Terminal {
	private var deferredConnection: CompletableDeferred<AndroidTerminalConnection>? = null
	var tag: IsoDep? = null

	@SuppressLint("NewApi")
	fun setNFCTag(tag: IsoDep) {
		this.tag = tag

		// nfcAdapter?.disableForegroundDispatch(androidActivity)
		deferredConnection?.complete(
			AndroidTerminalConnection(this),
		)
	}

	override fun isCardPresent() = getState() == TerminalStateType.PRESENT

	override fun getState() =
		when (tag) {
			is IsoDep -> {
				try {
					/*
					if connected we tag is PRESENT
					if not, we attempt to connect and disconnect to provoke exception if tag is gone
					if it is not thrown tag is still there
					 */
					val isConnected = tag?.isConnected
					if (isConnected == false) {
						tag?.connect()
						tag?.close()
					}
					TerminalStateType.PRESENT
				} catch (e: SecurityException) {
					tag = null
					TerminalStateType.ABSENT
				}
			}
			null -> TerminalStateType.ABSENT
		}

	override fun connectTerminalOnly() = AndroidTerminalConnection(this, false)

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): AndroidTerminalConnection =
		if (isCardPresent()) {
			connectTerminalOnly()
		} else {
			runBlocking {
				val activityIntent: Intent =
					Intent(androidActivity, androidActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				val flags = if (android.os.Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
				val pendingIntent: PendingIntent = PendingIntent.getActivity(androidActivity, 0, activityIntent, flags)

				deferredConnection = CompletableDeferred()

				nfcAdapter?.enableForegroundDispatch(androidActivity, pendingIntent, null, null)
				deferredConnection?.await()!!
			}
		}

	override suspend fun waitForCardPresent() {
		// dispatch einachslaten - höchstwahrscheinlich hier auch - dann aber managed
		TODO("Not yet implemented")
	}

	override suspend fun waitForCardAbsent() {
		TODO("Not yet implemented")
	}
}
