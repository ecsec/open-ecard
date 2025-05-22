package org.openecard.sal.iface.dids

import org.openecard.sal.iface.Cancelled
import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.NoService
import org.openecard.sal.iface.NotInitialized
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.SecureMessagingException
import org.openecard.sal.iface.SharingViolation
import org.openecard.sal.iface.Timeout
import org.openecard.sal.iface.UnsupportedFeature
import org.openecard.sc.iface.feature.PinError
import org.openecard.sc.iface.feature.PinStatus
import kotlin.coroutines.cancellation.CancellationException

interface PinDid : AuthenticationDid {
	val missingModifyAuthentications: MissingAuthentications

	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		RemovedDevice::class,
		UnsupportedFeature::class,
		Timeout::class,
		Cancelled::class,
	)
	fun capturePinInHardware(): Boolean

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
		PinError::class,
	)
	fun pinStatus(): PinStatus

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
		CancellationException::class,
		PinError::class,
	)
	suspend fun verify(pinCallback: PinCallback)

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
		CancellationException::class,
		PinError::class,
	)
	suspend fun verify()

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
		CancellationException::class,
		PinError::class,
	)
	suspend fun modify(
		oldPinCallback: PinCallback,
		newPinCallback: PinCallback,
	)

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
		CancellationException::class,
		PinError::class,
	)
	suspend fun modify()
}
