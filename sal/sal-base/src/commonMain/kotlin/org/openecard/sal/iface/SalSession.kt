package org.openecard.sal.iface

interface SalSession {
	val sal: Sal
	val sessionId: String

	@Throws(
		InternalSystemError::class,
		NoService::class,
	)
	fun initializeStack()

	@Throws(
		InternalSystemError::class,
		NotInitialized::class,
		NoService::class,
	)
	fun shutdownStack()

	@Throws(
		NotInitialized::class,
		NoService::class,
		Timeout::class,
		Cancelled::class,
	)
	fun devices(): List<String>

	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		UnsupportedFeature::class,
		RemovedDevice::class,
		Timeout::class,
		Cancelled::class,
	)
	fun connect(
		terminalName: String,
		isExclusive: Boolean = false,
	): DeviceConnection
}
