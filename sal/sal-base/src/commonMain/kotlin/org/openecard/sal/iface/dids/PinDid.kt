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
	val missingResetAuthentications: MissingAuthentications

	val supportedModifyModes: Set<ModifyMode>
	val supportsModifyWithoutOldPassword: Boolean
	val supportsModifyWithOldPassword: Boolean

	val supportedResetModes: Set<ResetMode>
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
	suspend fun modify(
		modifyMode: ModifyMode = preferredModifyMode(),
		lang: UsbLangId = DEFAULT_LANGUAGE,
	)

	fun PinDid.preferredModifyMode() =
		if (supportsModifyWithOldPassword) {
			PinDid.ModifyMode.WITH_OLD_PASSWORD
		} else {
			PinDid.ModifyMode.WITHOUT_OLD_PASSWORD
		}

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
	suspend fun modify(
		withOldPin: Boolean = supportsModifyWithOldPassword,
		lang: UsbLangId = DEFAULT_LANGUAGE,
	) {
		val mode = if (withOldPin) ModifyMode.WITH_OLD_PASSWORD else ModifyMode.WITHOUT_OLD_PASSWORD
		modify(mode, lang)
	}

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
	fun resetPassword(
		resetMode: ResetMode,
		lang: UsbLangId = DEFAULT_LANGUAGE,
	)

	/**
	 * Mode of the change reference data command.
	 */
	enum class ModifyMode {
		/**
		 * Password modify works with the old password, in order to change it (P1=00)
		 */
		WITH_OLD_PASSWORD,

		/**
		 * Password modify works without the old password, in order to change it (P1=01)
		 */
		WITHOUT_OLD_PASSWORD,
	}

	/**
	 * Mode of the reset retry counter command.
	 */
	enum class ResetMode {
		/**
		 * Password reset works without reference data (P1=03)
		 */
		WITHOUT_DATA,

		/**
		 * Password reset works with unblocking and reference data (P1=00)
		 */
		WITH_UNBLOCK_AND_PASSWORD,

		/**
		 * Password reset works with reference data (P1=02)
		 */
		WITH_PASSWORD,

		/**
		 * Password reset works with unblocking data (P1=01)
		 */
		WITH_UNBLOCK,
	}

	companion object {
		val DEFAULT_LANGUAGE = UsbLangId(UsbLang.ENGLISH_UNITED_STATES.code)
	}
}
