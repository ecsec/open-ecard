package org.openecard.demo

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory


private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun doPaceWithEgk(
	terminalFactory: TerminalFactory?,
//	tokenUrl: String,
// 	tokenUrl: String,
	can: String,
	nfcDetected: () -> Unit,
): Boolean {
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
//					terminal.waitForCardPresent()
//					nfcDetected()
//
//					val uiStep = EidActivation.startEacProcess(clientInfo, tokenUrl, session, terminal.name)
//
//					val paceResp =
//						uiStep.getPaceDid().establishChannel(
//							pin,
//							uiStep.guiData.optionalChat.asBytes,
//							uiStep.guiData.certificateDescription.asBytes,
//						)
//					val serverStep = uiStep.processAuthentication(paceResp)
//					when (val result = serverStep.processEidServerLogic()) {
//						is BindingResponse.RedirectResponse -> result.redirectUrl
//						else -> "failed result ${result.status}"
//					}
					// -------------------
					terminal.waitForCardPresent()
					nfcDetected()

					val con = session.connect(terminal.name)

					EgkCif.metadata.id == con.deviceType// { "Recognized card is not an eGK" }

					val mf = con.applications.find { it.name == EgkCifDefinitions.Apps.Mf.name }
					val app = con.applications.find { it.name == EgkCifDefinitions.Apps.ESign.name }
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
							"PACE should be the only DID needed for this DS"
							false
						}

						is MissingAuthentications.MissingDidAuthentications -> {
							val authOption = missing.options.first()
							authOption.size == 1
							val did = authOption.first().authDid
							when (did) {
								is PaceDid -> {
									!did.capturePasswordInHardware()
									did.establishChannel(can, null, null)
									true
								}

								else -> {
									"Non PACE DID found"
									false
								}
							}
						}
					}

//					app.connect()
//					val certData = certDs.read()
//					val certs =
//						CertificateFactory
//							.getInstance("X.509")
//							.generateCertificates(certData.toByteArray().inputStream())
//					certs.isNotEmpty()

//					mf?.connect()
//					val efDirDs = mf?.datasets?.find { it.name == "EF.DIR" }
//					val efDirData = efDirDs?.read()
//					efDirData?.isNotEmpty()


//					@OptIn(ExperimentalUnsignedTypes::class)
//					val personalData: PersoenlicheVersichertendaten by lazy {
//						connection.applications.find { it.name == EgkCifDefinitions.Apps.Hca.name }?.run {
//							readDataSet(EgkCifDefinitions.Apps.Hca.Datasets.efPd).toPersonalData()
//						} ?: readError(EgkCifDefinitions.Apps.Hca.Datasets.efPd)
//					}
				}
			}
		} ?: false


	} catch (e: Throwable) {
		logger.debug(e) { "Error" }
		e.message
		false
	}
}


