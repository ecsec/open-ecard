package org.openecard.sal.iface

import org.openecard.sal.iface.dids.Did

interface Application {
	val name: String
	val device: DeviceConnection

	val datasets: List<Dataset>
	val dids: List<Did>

	val missingSelectAuthentications: MissingAuthentications

	val isConnected: Boolean

	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		UnsupportedFeature::class,
		RemovedDevice::class,
		Timeout::class,
		Cancelled::class,
		MissingAuthentication::class,
		SecureMessagingException::class,
	)
	fun connect()
}
