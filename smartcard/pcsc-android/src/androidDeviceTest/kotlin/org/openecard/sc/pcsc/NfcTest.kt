package org.openecard.sc.pcsc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.iface.TerminalStateType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

// TODO: add android device test in smartcard-sal and copy EgkPaceTest for android device

class TestActivity : Activity() {
	var factory: AndroidTerminalFactory? = null
	var textView: TextView? = null

	fun msg(msg: String) =
		runOnUiThread {
			textView?.text = msg
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		factory = AndroidTerminalFactory(this)
		this.textView =
			TextView(this@TestActivity).apply {
				text = "running tests"
				textSize = 24f
				gravity = Gravity.CENTER
			}
		setContentView(
			FrameLayout(this).apply {
				addView(
					textView,
					FrameLayout
						.LayoutParams(
							FrameLayout.LayoutParams.WRAP_CONTENT,
							FrameLayout.LayoutParams.WRAP_CONTENT,
						).apply { gravity = Gravity.CENTER },
				)
			},
		)
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		intent?.let {
			factory?.tagIntentHandler(it)
		}
	}
}

@RunWith(AndroidJUnit4::class)
class NfcTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testConnect() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val connection =
								activity.factory
									?.load()
									?.androidTerminal
									?.connect()
							assertTrue(connection?.isCardConnected == true, "Card not connected")
							assertNotNull(connection.card?.atr) { "Atr could not be read" }
						}
				}

				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testReconnect() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							val androidTerminal = terminals?.androidTerminal
							assertEquals(TerminalStateType.ABSENT, androidTerminal?.getState())

							val connection = androidTerminal?.connect()
							assertEquals(TerminalStateType.PRESENT, androidTerminal?.getState())
							connection?.disconnect()
							// terminal still has tag
							assertEquals(TerminalStateType.PRESENT, androidTerminal?.getState())

							connection?.reconnect()
							assert(connection?.card?.atr != null) { "Atr could not be read after reconnect" }

							val connection2 = androidTerminal?.connectTerminalOnly()
							assert(connection2?.isCardConnected == true) { "Card not connected in second connection" }
							assert(connection2?.card?.atr != null) { "Atr could not be read in second connection" }
						}
				}

				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testTerminalState() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							val androidTerminal = terminals?.androidTerminal
							assertEquals(TerminalStateType.ABSENT, androidTerminal?.getState())

							val connection = androidTerminal?.connect()
							assertEquals(TerminalStateType.PRESENT, androidTerminal?.getState())

							connection?.disconnect()
							assertTrue(connection?.isCardConnected == false, "connection still says card is connected")
							assertEquals(TerminalStateType.PRESENT, androidTerminal.getState())

							activity.msg("remove the card")
							delay(5.seconds)

							assertEquals(TerminalStateType.ABSENT, androidTerminal.getState(), "Card is removed, thus")
						}
				}

				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testTransceive() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							val androidTerminal = terminals?.androidTerminal

							val connection = androidTerminal?.connect()
							assertTrue(connection?.isCardConnected == true)
							assertTrue(
								connection.card
									?.atr
									?.bytes
									?.isNotEmpty() == true,
								"Atr could not be read in connection",
							)
							assertEquals(true, connection.card?.tag?.isConnected)

							val resp = connection.card?.basicChannel?.transmit(Select.selectMf().apdu)
							assertEquals(StatusWord.OK, resp?.status?.type)
						}
				}
				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testAtrFromHistoricalBytes() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							val androidTerminal = terminals?.androidTerminal

							val connection = androidTerminal?.connect()
							assertTrue(connection?.isCardConnected == true)

							val histBytes = connection.card?.tag?.historicalBytes
							assertTrue { histBytes?.isNotEmpty() == true }

							println("HISTBYTES: ${histBytes?.toHexString()}")

							assertNotNull(
								connection.card?.atr?.historicalBytes,
								"Historcial Bytes in Atr must not be null",
							)
						}
				}
				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testWaitForCardPresent() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put card at device")
					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							val androidTerminal = terminals?.androidTerminal

							val connection = androidTerminal?.connectTerminalOnly()
							androidTerminal?.waitForCardPresent()

							androidTerminal?.connect()

							assertTrue(connection?.isCardConnected == true, "Card should be connected after waitForCard")
						}
				}
				j?.join()
			}
		}
	}
}
