@file:Suppress("ktlint:standard:filename")

package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.demo.domain.PinOperations
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardDeviceConnection

abstract class PinMgmtViewModel : ViewModel() {
	var pinOps: PinOperations? = null
	var pacePin: PaceDid? = null

	fun setConnection(connection: SmartcardDeviceConnection) {
		val mf =
			connection.applications.find { it.name == NpaDefinitions.Apps.Mf.name }
				?: throw IllegalStateException("MF not found")

		pacePin =
			mf.dids.filterIsInstance<PaceDid>().find {
				it.name == NpaDefinitions.Apps.Mf.Dids.pacePin
			} ?: throw IllegalStateException("PACE PIN DID not found")

		setConnectionForOperation(mf)
	}

	abstract fun setConnectionForOperation(mf: SmartcardApplication)
}
