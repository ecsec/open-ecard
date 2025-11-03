package org.openecard.sal.iface

import org.openecard.sal.iface.dids.AuthenticationDid
import org.openecard.sc.iface.CardDisposition

interface
DeviceConnection {
	val connectionId: String
	val session: SalSession
	val isExclusive: Boolean

	/**
	 * An identifier of the connected device.
	 * When dealing with smartcards, this is the card-type as defined in the CIF.
	 */
	val deviceType: String

	val applications: List<Application>
	val authenticatedDids: List<AuthenticationDid>

	@Throws(
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		InternalSystemError::class,
	)
	fun beginExclusive()

	@Throws(
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		InternalSystemError::class,
	)
	fun endExclusive()

	@Throws(
		InternalSystemError::class,
		NotInitialized::class,
		Timeout::class,
		Cancelled::class,
	)
	fun close(disposition: CardDisposition)
}
