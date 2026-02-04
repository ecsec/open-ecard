package org.openecard.sc.pcsc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import org.openecard.sc.iface.NoSmartcard
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ReaderUnsupported
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalStateType
import org.openecard.sc.iface.Terminals
import kotlin.jvm.java

private val logger = KotlinLogging.logger { }

@SuppressLint("NewApi")
class AndroidTerminal(
	override val terminals: Terminals,
	override val name: String,
	val androidActivity: Activity,
	val nfcAdapter: NfcAdapter?,
) : Terminal {
	private var deferredConnection: CompletableDeferred<Nothing?> = CompletableDeferred()
	var tag: IsoDep? = null

	/**
	 * The following flag is used to manage the switching of nfc-dispatch handling on android and bind it to the
	 * lifecycle states of the activity.
	 * The dispatch has to be switched on to get nfc-intents delivered to the app.
	 * If the activity gets paused, we have to switch it off and on again if resumed, if the sdk still wants to read a card.
	 * We therefore register lifecycleCallbacks enabling us to switch dispatch in correct lifecycle states.
	 *
	 * `waitingForTag` is set while the sdk wants to communicate with a card.
	 *  note that, if the sdk stops waiting during paused waitingForTag gets false and we don't switch the dispatch on in
	 *  onResume
	 *
	 */
	private var waitingForTag = false

	private val lifeCycleCallbacks =
		object : Application.ActivityLifecycleCallbacks {
			override fun onActivityCreated(
				activity: Activity,
				savedInstanceState: Bundle?,
			) = Unit

			override fun onActivityStarted(activity: Activity) = Unit

			override fun onActivityStopped(activity: Activity) = Unit

			override fun onActivitySaveInstanceState(
				activity: Activity,
				outState: Bundle,
			) = Unit

			override fun onActivityDestroyed(activity: Activity) = Unit

			override fun onActivityResumed(activity: Activity) {
				if (waitingForTag) {
					nfcTagDiscoveryOn()
				}
			}

			override fun onActivityPaused(activity: Activity) {
				nfcTagDiscoveryOff()
			}
		}

	private fun registerLifeCycleCallbacks() {
		androidActivity.registerActivityLifecycleCallbacks(lifeCycleCallbacks)
	}

	private fun unRegisterLifeCycleCallbacks() {
		androidActivity.unregisterActivityLifecycleCallbacks(lifeCycleCallbacks)
	}

	val tagIntentHandler: ((tag: Intent) -> Unit) = {
		waitingForTag = false
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
		deferredConnection.complete(null)
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

			null -> {
				TerminalStateType.ABSENT
			}
		}

	override fun connectTerminalOnly() = AndroidTerminalConnection(this)

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): AndroidTerminalConnection =
		if (isCardPresent()) {
			connectTerminalOnly().apply { connectTag() }
		} else {
			throw NoSmartcard()
		}

	override suspend fun waitForCardPresent() {
		deferredConnection.await()
	}

	/**
	 * TerminalOn can happen during the app being in resumed state, or in paused state.
	 * In the first case no `onResume` will happen which would switch the dispatch on in the lifeCycleCallbacks,
	 * thus we do it directly by calling `nfcTagDiscoveryOn()`
	 * This however may lead to an IllegalStateException if we are in the paused state, which cannot be
	 * determined beforehand.
	 * In this case we catch n ignore that exception to avoid crashes.
	 * The dispatch will get enabled in `onResume` in the `lifeCycleCallbacks` later.
	 */
	internal fun terminalOn() {
		registerLifeCycleCallbacks()
		waitingForTag = true
		try {
			nfcTagDiscoveryOn()
		} catch (e: IllegalStateException) {
			logger.warn {
				"Catch and ignore ${e.message} due to activity being paused." +
					" Nfc will get switched on when resumed."
			}
		}
	}

	internal fun terminalOff() {
		unRegisterLifeCycleCallbacks()
		waitingForTag = false
		deferredConnection = CompletableDeferred()
		tag = null
		nfcTagDiscoveryOff()
	}

	internal fun nfcTagDiscoveryOff() {
		nfcAdapter?.disableForegroundDispatch(androidActivity)
	}

	internal fun nfcTagDiscoveryOn() {
		val activityIntent: Intent =
			Intent(androidActivity, androidActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		val flags = if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
		val pendingIntent: PendingIntent = PendingIntent.getActivity(androidActivity, 0, activityIntent, flags)

		nfcAdapter?.enableForegroundDispatch(androidActivity, pendingIntent, null, null)
	}

	override suspend fun waitForCardAbsent() {
		TODO("Not yet implemented")
	}
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
	when {
		Build.VERSION.SDK_INT >= 33 -> {
			getParcelableExtra(key, T::class.java)
		}

		else -> {
			@Suppress("DEPRECATION")
			getParcelableExtra(key) as? T
		}
	}
