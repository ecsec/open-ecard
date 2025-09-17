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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.openecard.sc.iface.Cancelled
import org.openecard.sc.iface.NoSmartcard
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ReaderUnsupported
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalStateType
import org.openecard.sc.iface.Terminals
import kotlin.jvm.java
import kotlin.random.Random

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
	 * The following flags are used to manage the switching of nfc-dispatch handling on android and bind it to the
	 * lifecycle states of the activity.
	 * The dispatch has to be switched on to get nfc-intents delivered to the app.
	 * If the activity gets paused, we switch it off and on again if resumed, if the sdk still wants to read a card.
	 *
	 * However, switching the dispatch is only allowed while the activity is in resumed state and
	 * there is no way to request if the activity is in resumed state directly.
	 * To get this information we add the lifecycleCallbacks enabling us to switch dispatch in correct lifecycle states.
	 *
	 * `waitingForTag` is set while the sdk wants to communicate with a card.
	 * `resumeNfc` is set if during `waitingForTag` we get paused and have to switch dispatch on again onResume.
	 *  note that, if the sdk stops waiting during paused (waitingForTag gets false),
	 *  we also don't switch dispatch on again onResume
	 *
	 * There is a second place where `resumeNfc` can be set to true, which is the `terminalOn` function in the case that
	 * an IllegalstateException is thrown, which happens if we try to switch the dispatch on while the activity is paused.
	 * This might happen, since the `lifeCycleCallbacks` monitoring the activity state are only in effect while the sdk
	 * wants to read a card. In the moment `terminalOn` is called, we cannot know in which state the activity is, thus
	 * it might happen that we try to enable the dispatch during paused state. In this case we catch the thrown exception and
	 * set the resumeNfc flag to postpone a second attempt when the activity gets resumed again.
	 */
	private var waitingForTag = false
	private var resumeNfc = false

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
				if (waitingForTag && resumeNfc) {
					nfcTagDiscoveryOn()
					resumeNfc = false
				}
			}

			override fun onActivityPaused(activity: Activity) {
				if (waitingForTag) {
					resumeNfc = true
					nfcTagDiscoveryOff()
				}
			}
		}

	private val random = Random(0)

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
			throw NoSmartcard()
		}

	override suspend fun waitForCardPresent() {
		deferredConnection.await()
	}

	internal fun terminalOn() {
		registerLifeCycleCallbacks()
		waitingForTag = true
		try {
			nfcTagDiscoveryOn()
		} catch (e: IllegalStateException) {
			resumeNfc = true
			logger.warn {
				"Catch and ignore ${e.message} due to activity being paused." +
					" Nfc will get switched on when resumed."
			}
		}
	}

	internal fun terminalOff() {
		unRegisterLifeCycleCallbacks()
		waitingForTag = false
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
		Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
		else ->
			@Suppress("DEPRECATION")
			getParcelableExtra(key) as? T
	}
