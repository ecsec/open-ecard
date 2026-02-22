package org.openecard.demo.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.demo.data.SalStackFactory
import org.openecard.demo.util.toPersonalData
import org.openecard.demo.viewmodel.EgkViewModel
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sc.iface.feature.PaceError

private val logger = KotlinLogging.logger { }

class EgkOperations(
	val session: SmartcardSalSession,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun doPace(
		egkViewModel: EgkViewModel,
		nfcDetected: () -> Unit,
		can: String,
	): String? {
		val ops = egkViewModel.egkOps ?: return "Session not initialized"

		val terminal = SalStackFactory.initializeNfcStack(session, nfcDetected)

		val connection = ops.session.connect(terminal.name)
		egkViewModel.connection = connection

		val eSignApp =
			connection.applications
				.find { it.name == EgkCifDefinitions.Apps.ESign.name }
				?: return "ESIGN app not found"

		val certDs =
			eSignApp.datasets.find { it.name == "EF.C.CH.AUT.E256" }
				?: return "EF.C.CH.AUT.E256 data set not found"

		val missing =
			certDs.missingReadAuthentications.removeUnsupported(
				listOf(
					DidStateReference.forName(EgkCifDefinitions.Apps.Mf.Dids.autPace),
				),
			)

		return when (missing) {
			MissingAuthentications.Unsolveable -> {
				"Missing authentication unsolvable"
			}

			is MissingAuthentications.MissingDidAuthentications -> {
				val did =
					missing.options
						.first()
						.first()
						.authDid
				if (did is PaceDid) {
					try {
						did.establishChannel(can, null, null)
						return null
					} catch (e: PaceError) {
						logger.error(e) { "Could not establish channel" }
						return "Wrong CAN or invalid card state"
					} catch (e: Exception) {
						logger.error(e) { "Could not establish channel" }
						return e.message
					}
				} else {
					logger.error { "PACE DID not found" }
					return "PACE DID not found"
				}
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun readPersonalData(egkViewModel: EgkViewModel): String? {
		val connection = egkViewModel.connection ?: return "Could not establish connection"

		val hcaApp =
			connection.applications
				.find { it.name == EgkCifDefinitions.Apps.Hca.name }
				?: return "HCA app not found"

		hcaApp.connect()

		val pdDataSet =
			hcaApp.datasets
				.find { it.name == EgkCifDefinitions.Apps.Hca.Datasets.efPd }
				?: return "EF.PD data set not found"

		val pd = pdDataSet.read()
		val person = pd.toPersonalData()?.versicherter?.person

		return person?.let { "Hello ${it.vorname} ${it.nachname}" }
	}
}
