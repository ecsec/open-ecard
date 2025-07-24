package org.openecard.sc.pcsc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Parcelable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ReaderUnsupported
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
	private var deferredConnection: CompletableDeferred<Nothing?>? = null
	var tag: IsoDep? = null

	val tagIntentHandler: ((tag: Intent) -> Unit) = {
		val isoDep = IsoDep.get(it.parcelable<Tag>(NfcAdapter.EXTRA_TAG))

		if (isoDep != null) {
			if (isoDep.isExtendedLengthApduSupported) {
				setNFCTag(isoDep)
			} else {
				throw ReaderUnsupported("APDU Extended Length is not supported.")
			}
		} else {
			logger.warn { "Given intent didn't carry a supported tag." }
		}
	}

	@SuppressLint("NewApi")
	fun setNFCTag(tag: IsoDep) {
		this.tag = tag

		// TODO: Do we have to handle this here or somewhere else
		// nfcAdapter?.disableForegroundDispatch(androidActivity)
		deferredConnection?.complete(null)
	}

	override fun isCardPresent() = getState() == TerminalStateType.PRESENT

	override fun getState() =
		when (val localTag = tag) {
			is IsoDep -> {
				try {
					/*
					if connected we return tag is PRESENT
					if not, we try to connect and disconnect to provoke exception if tag is gone
					if it is not thrown tag is still there
					if tag isConnected it might be lost without beeing detected yet, which will lead to errors
					for the caller which connected, which has to handle it
					 */
					if (!localTag.isConnected) {
						localTag.connect()
						localTag.close()
					}
					TerminalStateType.PRESENT
				} catch (e: Exception) {
					if (localTag == tag) {
						tag = null
					}
					TerminalStateType.ABSENT
				}
			}
			null -> TerminalStateType.ABSENT
		}

	override fun connectTerminalOnly() = AndroidTerminalConnection(this)

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): AndroidTerminalConnection =
		if (isCardPresent()) {
			connectTerminalOnly().apply { connectTag() }
		} else {
			runBlocking(Dispatchers.IO) {
				waitForCardPresent()
				connectTerminalOnly().apply { connectTag() }
			}
		}

	override suspend fun waitForCardPresent() {
		val activityIntent: Intent =
			Intent(androidActivity, androidActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		val flags = if (android.os.Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
		val pendingIntent: PendingIntent = PendingIntent.getActivity(androidActivity, 0, activityIntent, flags)

		deferredConnection = CompletableDeferred()

		nfcAdapter?.enableForegroundDispatch(androidActivity, pendingIntent, null, null)
		deferredConnection?.await()
	}

	override suspend fun waitForCardAbsent() {
		TODO("Not yet implemented")
	}
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
	when {
		Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
		else ->
			@Suppress("DEPRECATION")
			getParcelableExtra(key) as? T
	}
