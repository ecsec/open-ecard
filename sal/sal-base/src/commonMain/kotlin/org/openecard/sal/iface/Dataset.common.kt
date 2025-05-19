package org.openecard.sal.iface

interface Dataset {
	val name: String
	val application: Application

	val missingAuthentications: MissingAuthentications

	// TODO: support Records and other file things

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
	@OptIn(ExperimentalUnsignedTypes::class)
	fun read(): UByteArray

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
	@OptIn(ExperimentalUnsignedTypes::class)
	fun write(): UByteArray
}
