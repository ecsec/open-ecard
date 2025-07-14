package org.openecard.sal.iface.dids

import org.openecard.sal.iface.Application
import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.NoService
import org.openecard.sal.iface.NotInitialized
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.SecureMessagingException
import org.openecard.sal.iface.SharingViolation

sealed interface Did {
	val name: String
	val application: Application

	val isLocal: Boolean
}

sealed interface AuthenticationDid : Did {
	val missingAuthAuthentications: MissingAuthentications
}

sealed interface SecureChannelDid : Did {
	/**
	 * Close the secure channel established by this DID.
	 * This may be a no-op if the channel can not be closed.
	 */
	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		RemovedDevice::class,
		SecureMessagingException::class,
	)
	fun closeChannel()
}

fun List<Did>.pinDids(): List<PinDid> = filterIsInstance<PinDid>()

fun List<Did>.paceDids(): List<PaceDid> = filterIsInstance<PaceDid>()

fun List<Did>.signDids(): List<SignDid> = filterIsInstance<SignDid>()
