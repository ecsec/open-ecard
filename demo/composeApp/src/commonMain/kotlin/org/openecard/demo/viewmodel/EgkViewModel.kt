package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import org.openecard.demo.data.logger
import org.openecard.demo.model.ConnectEgk
import org.openecard.sc.iface.TerminalFactory

class EgkViewModel(
	private val terminalFactory: TerminalFactory?
) : ViewModel() {

	suspend fun readEgk(
		nfcDetected: () -> Unit,
		can: String
	): String? {
		return try {
			val model = terminalFactory?.let { ConnectEgk.createConnectedModel(it, nfcDetected) }

			if (model != null) {
				val paceOk = model.doPace(can)

				if (!paceOk) {
					return "Wrong CAN"
				}

				model.readPersonalData()
			} else {
				logger.error { "Could not connect card." }
				return null
			}
		} catch (e: Exception) {
			logger.error(e) { "PACE operation failed." }
			e.message
		}
	}
}
