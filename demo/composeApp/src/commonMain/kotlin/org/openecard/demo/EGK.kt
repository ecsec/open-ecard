package org.openecard.demo

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory


private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalUnsignedTypes::class)
private fun SmartcardApplication.readDataSet(name: String): UByteArray? =
	datasets.find { it.name == name }?.run {
		this@readDataSet.connect()
		read()
	} //readError(name)

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun doPaceWithEgk(
	terminalFactory: TerminalFactory?,
	can: String,
	nfcDetected: () -> Unit,
): String? {
	return try {

		terminalFactory?.load()?.withContextSuspend { ctx ->
			val recognition =
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType)))
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(EgkCif), recognition, paceFactory)

			val session = sal.startSession()

			when (val terminal = ctx.getTerminal("")) {
				null -> {
					throw Error("NO TERMINAL")
				}

				else -> {
					terminal.waitForCardPresent()
					nfcDetected()

					val con = session.connect(terminal.name)

					EgkCif.metadata.id == con.deviceType

					val mf = con.applications.find { it.name == EgkCifDefinitions.Apps.Mf.name }
					val app = con.applications.find { it.name == EgkCifDefinitions.Apps.ESign.name }

					val hcaApp = con.applications.find { it.name == EgkCifDefinitions.Apps.Hca.name }

					mf?.connect()

					val certDs = app?.datasets?.find { it.name == "EF.C.CH.AUT.E256" }
					!certDs?.missingReadAuthentications?.isSolved!!
					val missing =
						certDs.missingReadAuthentications
							.removeUnsupported(
								listOf(
									DidStateReference.forName(EgkCifDefinitions.Apps.Mf.Dids.autPace),
								),
							)
					when (missing) {
						MissingAuthentications.Unsolveable -> {
							null
						}

						is MissingAuthentications.MissingDidAuthentications -> {
							val authOption = missing.options.first()
							authOption.size == 1
							val did = authOption.first().authDid
							when (did) {
								is PaceDid -> {
									!did.capturePasswordInHardware()
									did.establishChannel(can, null, null)
									hcaApp?.connect()

									val pdDataSet =
										hcaApp?.datasets?.find { it.name == EgkCifDefinitions.Apps.Hca.Datasets.efPd }

									val pd = pdDataSet?.read()

									logger.debug { "personal data: ${pd?.toPersonalData()?.versicherter?.person?.nachname}" }
//									listOf(
//										pd?.toPersonalData()?.versicherter?.person?.vorname,
//										pd?.toPersonalData()?.versicherter?.person?.nachname,
//									).toString()
									val person = pd?.toPersonalData()?.versicherter?.person
									"Hello ${person?.vorname} ${person?.nachname}"
								}

								else -> {
									throw Error("NO PACE DID")
								}
							}
						}
					}
				}
			}
		}


	} catch (e: Throwable) {
		logger.debug(e) { "Error" }
		e.message
	}
}


