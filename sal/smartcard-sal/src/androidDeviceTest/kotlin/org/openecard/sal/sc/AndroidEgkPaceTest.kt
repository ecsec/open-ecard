package org.openecard.sal.sc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.AndroidTerminalFactory
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class TestActivity : Activity() {
	var factory: AndroidTerminalFactory? = null
	var textView: TextView? = null

	fun msg(msg: String) =
		runOnUiThread {
			textView?.text = msg
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		factory = AndroidTerminalFactory.instance(this)
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

const val EGK_CAN = "123123"

@RunWith(AndroidJUnit4::class)
class AndroidEgkPaceTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun execute_pace_with_can() {
		runBlocking {
			var j: Job? = null
			launchActivity<TestActivity>().use { scenario ->
				scenario.onActivity { activity ->
					assert(activity.factory?.nfcAvailable == true) {
						"NFC not available"
					}
					assert(activity.factory?.nfcEnabled == true) {
						"NFC not enabled"
					}

					activity.msg("Put eGK card to device")

					j =
						CoroutineScope(Dispatchers.IO).launch {
							val terminals =
								activity.factory
									?.load()

							assertNotNull(terminals)
							
							val recognition = DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType)))
							val paceFactory = PaceFeatureSoftwareFactory()
							val sal = SmartcardSal(terminals, setOf(EgkCif), recognition, paceFactory)

							val session = sal.startSession()
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
											runBlocking { did.establishChannel(EGK_CAN, null, null) }
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
				j?.join()
			}
		}
	}
}
