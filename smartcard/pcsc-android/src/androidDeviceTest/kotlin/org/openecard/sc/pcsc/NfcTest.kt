package org.openecard.sc.pcsc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.runner.RunWith
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.iface.TerminalStateType
import org.openecard.sc.iface.withContextSuspend
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private val logger = KotlinLogging.logger { }

class TestActivity : Activity() {
	var factory: AndroidTerminalFactory? = null
	var textView: TextView? = null

	/**
	 * In a test below we want to check if nfc-stack is working after a user pauses and resumes the app
	 * Since "onNewIntent" causes a pause/resume we have to make sure the we don't set this flag if resumption
	 * was caused by "onNewIntent"
	 */
	var wasPaused = false
	var wasResumedAfterPaused = false
	var ignoreResumeByNewIntent = false

	fun msg(msg: String) =
		runOnUiThread {
			textView?.text = msg
		}

	override fun onPause() {
		super.onPause()
		wasPaused = true
	}

	override fun onResume() {
		super.onResume()
		if (wasPaused && !ignoreResumeByNewIntent) {
			wasResumedAfterPaused = true
		}
		ignoreResumeByNewIntent = false
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		factory = AndroidTerminalFactory.instance(this)
		this.textView =
			TextView(this@TestActivity).apply {
				text = "Running tests. - Nothing to do for you now - stay tuned."
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
		ignoreResumeByNewIntent = true
		intent?.let {
			factory?.tagIntentHandler(it)
		}
	}
}

@RunWith(AndroidJUnit4::class)
class NfcTest {
	private val testTimeout = 10.seconds

	@BeforeAll
	fun assureNfcOn() {
		runBlocking {
			launchActivity<TestActivity>().use {
				it.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}
				}
			}
		}
	}

	private suspend fun countDown(
		activity: TestActivity,
		testInstructions: String,
		timeout: Duration = testTimeout,
		checkInBetween: () -> Boolean = { true },
		whenOver: suspend () -> Unit = {},
	) {
		for (i in timeout.toInt(DurationUnit.SECONDS) downTo 1) {
			activity.msg(
				testInstructions +
					"\n\n $i secs left",
			)
			if (!checkInBetween()) {
				break
			}
			delay(1.seconds)
		}
		whenOver()
	}

	private fun CoroutineScope.connectWithTimeout(
		activity: TestActivity,
		testInstructions: String = "Bring card to device",
		block: suspend (connection: AndroidTerminalConnection) -> Unit,
	): Job {
		val countDown =
			launch {
				countDown(activity, testInstructions, testTimeout) {
					fail("Card not connected within $testTimeout")
				}
			}

		return launch {
			activity.factory
				?.load()
				?.withContextSuspend { terminals ->
					val androidTerminal = terminals.androidTerminal
					androidTerminal.waitForCardPresent()
					countDown.cancelAndJoin()
					block(androidTerminal.connect())
				}
		}
	}

	private fun runBackgroundTestJobWithActivity(testJob: CoroutineScope.(activity: TestActivity) -> Job) {
		runBlocking(Dispatchers.IO) {
			launchActivity<TestActivity>().use { scenario ->
				var j: Job? = null
				scenario.onActivity { activity ->
					j = testJob(activity)
				}
				j?.join()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testConnect() {
		runBackgroundTestJobWithActivity { activity ->
			connectWithTimeout(activity) { connection ->
				assertTrue(connection.isCardConnected, "Card not connected")
				assertNotNull(connection.card?.atr) { "Atr could not be read" }
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testTerminalState() {
		runBackgroundTestJobWithActivity { activity ->
			connectWithTimeout(activity) { connection ->
				assertTrue(connection.isCardConnected, "Card not connected")

				val androidTerminal = connection.terminal
				assertEquals(TerminalStateType.PRESENT, androidTerminal.getState())

				connection.disconnect()
				// terminal still has connected tag
				assertEquals(TerminalStateType.PRESENT, androidTerminal.getState())

				countDown(activity, "Remove the card.", 3.seconds) {
					// note that androidTermina.getState() will cause connect attempt which when fails sets the state
					// to ABSENT (no active monitoring)
					assertEquals(
						TerminalStateType.ABSENT,
						androidTerminal.getState(),
						"Card should have been absent now.",
					)
				}
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testTransceive() {
		runBackgroundTestJobWithActivity { activity ->
			connectWithTimeout(activity) { connection ->
				assertEquals(
					StatusWord.OK,
					connection.card
						?.basicChannel
						?.transmit(Select.selectMf().apdu)
						?.status
						?.type,
				)
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun testAtrFromHistoricalBytes() {
		runBackgroundTestJobWithActivity { activity ->
			connectWithTimeout(activity) { connection ->
				assertNotNull(
					connection.card?.atr?.historicalBytes,
					"Historical Bytes in parsed Atr must not be null",
				)
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun toggle_terminal_without_discovering_to_see_if_dispatch_disabling_works_without_crash() {
		runBackgroundTestJobWithActivity { activity ->
			launch(Dispatchers.IO) {
				var terminalsRef: AndroidTerminals? = null
				activity.factory?.load()?.withContextSuspend { terminals ->
					// dispatch is on through withContextSuspend
					assertTrue { terminals.isEstablished }
					terminalsRef = terminals
				} ?: fail("Could not establish context")
				assertTrue { terminalsRef?.isEstablished == false }
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun test_pause_and_resume_activity_before_connect() {
		runBackgroundTestJobWithActivity { activity ->
			launch(Dispatchers.IO) {
				activity.factory?.load()?.withContextSuspend { terminals ->
					// dispatch is on through withContextSuspend
					countDown(
						activity,
						"Pause and resume activity",
						checkInBetween = { !activity.wasResumedAfterPaused },
					) {
						assertTrue(
							activity.wasResumedAfterPaused,
							"Activity was not paused and resumed.",
						)

						val countDown =
							launch {
								countDown(activity, "Bring card to device.") {
									fail("Card not connected within $testTimeout")
								}
							}

						terminals.androidTerminal.waitForCardPresent()
						countDown.cancelAndJoin()
						assertTrue { terminals.androidTerminal.connect().isCardConnected }
					}
				} ?: fail("Could not establish context")
			}
		}
	}
}
