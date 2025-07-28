package org.openecard.sal.sc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.fail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.runner.RunWith
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.AndroidTerminalFactory
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class TestActivity : Activity() {
	var factory: AndroidTerminalFactory? = null
	var textView: TextView? = null

	var canField: EditText? = null
	var okBtn: Button? = null
	val can = CompletableDeferred<String>()

	fun msg(msg: String) =
		runOnUiThread {
			textView?.text = msg
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		factory = AndroidTerminalFactory.instance(this)

		canField =
			EditText(this).apply {
				text = SpannableStringBuilder("123123")
				inputType = InputType.TYPE_CLASS_TEXT
				gravity = Gravity.CENTER
			}

		textView =
			TextView(this@TestActivity).apply {
				text = "Edit can and confirm."
				textSize = 24f
				gravity = Gravity.CENTER
			}
		okBtn =
			Button(this).apply {
				text = "OK"
				textSize = 24f
				gravity = Gravity.CENTER
				setOnClickListener {
					can.complete(canField?.text.toString())
					isEnabled = false
					msg("Bring card to device.")
				}
			}

		setContentView(
			LinearLayout(this).apply {
				orientation = LinearLayout.VERTICAL
				gravity = Gravity.CENTER

				addView(textView)
				addView(canField)
				addView(okBtn)
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
class AndroidEgkPaceTest {
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
	fun execute_pace_with_can() {
		runBackgroundTestJobWithActivity { activity ->
			launch {
				activity.factory?.load()?.withContextSuspend { terminals ->

					assertNotNull(terminals)

					val recognition = DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType)))
					val paceFactory = PaceFeatureSoftwareFactory()
					val sal = SmartcardSal(terminals, setOf(EgkCif), recognition, paceFactory)

					val session = sal.startSession()
					terminals.androidTerminal.waitForCardPresent()
					val con = session.connect(terminals.androidTerminal.name)

					assert(EgkCif.metadata.id == con.cardType) { "Recognized card is not an eGK" }

					val mf = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.Apps.Mf.name })
					val app = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.Apps.ESign.name })
					mf.connect()

					val certDs = assertNotNull(app.datasets.find { it.name == "EF.C.CH.AUT.E256" })
					assert(!certDs.missingReadAuthentications.isSolved)
					val missing =
						certDs.missingReadAuthentications
							.removeUnsupported(
								listOf(
									DidStateReference.forName(EgkCifDefinitions.Apps.Mf.Dids.autPace),
								),
							)
					when (missing) {
						MissingAuthentications.Unsolveable -> fail("PACE should be the only DID needed for this DS")
						is MissingAuthentications.MissingDidAuthentications -> {
							val authOption = missing.options.first()
							assert(authOption.size == 1)
							val did = authOption.first().authDid
							when (did) {
								is PaceDid -> {
									assert(!did.capturePasswordInHardware())
									runBlocking { did.establishChannel(activity.can.await(), null, null) }
								}
								else -> assertFails { "Non PACE DID found" }
							}
						}
					}

					app.connect()
					val certData = certDs.read()
					val certs = CertificateFactory.getInstance("X.509").generateCertificates(certData.toByteArray().inputStream())
					assert(certs.isNotEmpty())

					mf.connect()
					val efDirDs = assertNotNull(mf.datasets.find { it.name == "EF.DIR" })
					val efDirData = efDirDs.read()
					assert(efDirData.isNotEmpty())
				}
			}
		}
	}
}
