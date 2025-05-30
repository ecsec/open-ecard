package org.openecard.sal.iface.dids

import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.NoService
import org.openecard.sal.iface.NotInitialized
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.SecureMessagingException
import org.openecard.sal.iface.SharingViolation

interface SignDid : Did {
	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		RemovedDevice::class,
		SecureMessagingException::class,
	)
	fun sign(data: ByteArray): ByteArray
}
