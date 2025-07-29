package org.openecard.sal.iface.dids

import org.openecard.sal.iface.Cancelled
import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.NoService
import org.openecard.sal.iface.NotInitialized
import org.openecard.sal.iface.PasswordError
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.SharingViolation
import org.openecard.sal.iface.Timeout
import org.openecard.sal.iface.UnsupportedFeature
import org.openecard.sc.apdu.command.SecurityCommandResult
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.feature.PaceEstablishChannelResponse
import org.openecard.sc.iface.feature.PacePinId
import kotlin.coroutines.cancellation.CancellationException

interface PaceDid :
	AuthenticationDid,
	SecureChannelDid {
	val pinType: PacePinId

	@Throws(
		NotInitialized::class,
		NoService::class,
		DeviceUnavailable::class,
		SharingViolation::class,
		RemovedDevice::class,
		UnsupportedFeature::class,
		Timeout::class,
		Cancelled::class,
		PaceError::class,
	)
	fun capturePasswordInHardware(): Boolean

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
		PaceError::class,
	)
	fun passwordStatus(): SecurityCommandResult

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
		PaceError::class,
		CancellationException::class,
		PasswordError::class,
	)
	@OptIn(ExperimentalUnsignedTypes::class)
	fun establishChannel(
		password: String,
		chat: UByteArray?,
		certDesc: UByteArray?,
	): PaceEstablishChannelResponse

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
		PaceError::class,
		CancellationException::class,
	)
	@OptIn(ExperimentalUnsignedTypes::class)
	suspend fun establishChannel(
		chat: UByteArray?,
		certDesc: UByteArray?,
	): PaceEstablishChannelResponse
}

sealed interface PinRequestResult

class PinRequestResultOk(
	val pin: String,
) : PinRequestResult

fun String.toPinRequestResult(): PinRequestResultOk = PinRequestResultOk(this)

object PinRequestResultUserCancel : PinRequestResult
