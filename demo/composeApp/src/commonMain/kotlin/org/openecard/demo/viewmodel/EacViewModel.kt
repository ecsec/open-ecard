package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
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
			val model = terminalFactory?.let {
				ConnectNpaEac.createEacModel(it, nfcDetected)
			} ?: return null

			model.doEac(tokenUrl, pin)

		} catch (e: Exception) {
			e.message
		}
	}
}
