package org.openecard.demo.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.demo.utils.toPersonalData
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardDeviceConnection

private val logger = KotlinLogging.logger { }

class EgkOperations(
	private val connection: SmartcardDeviceConnection,
) {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun doPace(can: String): Boolean {
		val eSignApp = connection.applications
			.find { it.name == EgkCifDefinitions.Apps.ESign.name }
			?: return false

		val certDs = eSignApp.datasets.find { it.name == "EF.C.CH.AUT.E256" }
			?: return false

		val missing = certDs.missingReadAuthentications.removeUnsupported(
			listOf(
				DidStateReference.forName(EgkCifDefinitions.Apps.Mf.Dids.autPace)
			)
		)

		return when (missing) {
			MissingAuthentications.Unsolveable -> false

			is MissingAuthentications.MissingDidAuthentications -> {
				val did = missing.options.first().first().authDid
				if (did is PaceDid) {
					try {
						did.establishChannel(can, null, null)
						true
					} catch (e: Exception) {
						logger.error(e) { "Could not establish channel." }
						false
					}
				} else {
					logger.error { "PACE DID not found." }
					false
				}
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun readPersonalData(): String? {
		val hcaApp = connection.applications
			.find { it.name == EgkCifDefinitions.Apps.Hca.name }
			?: return null

		hcaApp.connect()

		val pdDataSet = hcaApp.datasets
			.find { it.name == EgkCifDefinitions.Apps.Hca.Datasets.efPd }
			?: return null

		val pd = pdDataSet.read()
		val person = pd.toPersonalData()?.versicherter?.person

		return person?.let { "Hello ${it.vorname} ${it.nachname}" }
	}

	fun shutdownStack() {
		connection.session.shutdownStack()
	}
}
