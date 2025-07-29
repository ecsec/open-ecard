package org.openecard.sal.iface

import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sc.iface.CardDisposition

interface
DeviceConnection {
	val connectionId: String
	val session: SalSession

	val applications: List<Application>
	val authenticatedDids: List<AuthenticationDid>

	@Throws(
		InternalSystemError::class,
		NotInitialized::class,
		Timeout::class,
		Cancelled::class,
	)
	fun close(disposition: CardDisposition)
}
