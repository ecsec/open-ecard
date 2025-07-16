package org.openecard.sal.iface.dids

import org.openecard.sal.iface.Cancelled
import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.NoService
import org.openecard.sal.iface.NotInitialized
import org.openecard.sal.iface.PasswordError
import org.openecard.sal.iface.RemovedDevice
import org.openecard.sal.iface.SecureMessagingException
import org.openecard.sal.iface.SharingViolation
import org.openecard.sal.iface.Timeout
import org.openecard.sal.iface.UnsupportedFeature
import org.openecard.sc.iface.feature.PinCommandError
import org.openecard.sc.iface.feature.PinStatus
import org.openecard.sc.utils.UsbLang
import org.openecard.sc.utils.UsbLangId
import kotlin.coroutines.cancellation.CancellationException

interface PinDid : AuthenticationDid {
	val missingModifyAuthentications: MissingAuthentications

	val supportsResetWithoutData: Boolean
	val supportsResetWithPassword: Boolean
	val supportsResetWithUnblocking: Boolean
	val supportsResetWithUnblockingAndPassword: Boolean

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
	fun verifyPasswordInHardware(): Boolean

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
	fun modifyPasswordInHardware(): Boolean

	fun needsOldPasswordForChange(): Boolean

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
		PinCommandError::class,
	)
	fun passwordStatus(): PinStatus

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
		PinCommandError::class,
		PasswordError::class,
	)
	fun verify(password: String)

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
		PinCommandError::class,
	)
	suspend fun verify(lang: UsbLangId = DEFAULT_LANGUAGE)

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
		PinCommandError::class,
	)
	fun modify(
		newPassword: String,
		oldPassword: String?,
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
		PinCommandError::class,
	)
	suspend fun modify(lang: UsbLangId = DEFAULT_LANGUAGE)

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
		PinCommandError::class,
	)
	fun resetPassword(
		unblockingCode: String?,
		newPassword: String?,
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
		PinCommandError::class,
	)
	fun resetPassword(lang: UsbLangId = DEFAULT_LANGUAGE)

	companion object {
		val DEFAULT_LANGUAGE = UsbLangId(UsbLang.ENGLISH_UNITED_STATES.code)
	}
}
