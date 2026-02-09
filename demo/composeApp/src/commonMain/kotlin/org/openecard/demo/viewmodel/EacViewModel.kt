package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import org.openecard.demo.data.logger
import org.openecard.demo.model.ConnectNpaEac
import org.openecard.sc.iface.TerminalFactory

class EacViewModel(
	private val terminalFactory: TerminalFactory?
) : ViewModel() {

	suspend fun doEac(
		nfcDetected: () -> Unit,
		tokenUrl: String,
		pin: String
	): String? {
		return try {
			val model = terminalFactory?.let { ConnectNpaEac.createEacModel(it, nfcDetected) }

			if (model != null) {
				model.doEac(tokenUrl, pin)
			} else {
				logger.error { "Could not connect card." }
				return null
			}
		} catch (e: Exception) {
			logger.error(e) { "EAC operation failed." }
			e.message
		}
	}
}
