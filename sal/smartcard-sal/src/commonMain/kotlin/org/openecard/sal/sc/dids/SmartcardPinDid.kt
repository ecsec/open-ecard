package org.openecard.sal.sc.dids

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.did.DidScope
import org.openecard.cif.definition.did.PasswordEncodingDefinition
import org.openecard.cif.definition.did.PasswordFlags
import org.openecard.cif.definition.did.PasswordType
import org.openecard.cif.definition.did.PinDidDefinition
import org.openecard.sal.iface.MissingAuthentication
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.mapSmartcardError
import org.openecard.sc.apdu.command.ChangeReferenceData
import org.openecard.sc.apdu.command.ResetRetryCounter
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.Verify
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature
import org.openecard.sc.iface.feature.ModifyPinFeature
import org.openecard.sc.iface.feature.PasswordAttributes
import org.openecard.sc.iface.feature.PinCommandError
import org.openecard.sc.iface.feature.PinStatus
import org.openecard.sc.iface.feature.PinUtils
import org.openecard.sc.iface.feature.VerifyPinFeature
import org.openecard.sc.iface.feature.toPinStatusOrThrow
import org.openecard.sc.utils.UsbLangId
import org.openecard.utils.common.throwIf

class SmartcardPinDid(
	application: SmartcardApplication,
	didDef: PinDidDefinition,
	val authAcl: CifAclOr,
	val modifyAcl: CifAclOr,
	val resetAcl: CifAclOr,
) : SmartcardDid.BaseSmartcardDid<PinDidDefinition>(didDef, application),
	PinDid {
	override val missingAuthAuthentications: MissingAuthentications
		get() = missingAuthentications(authAcl)
	override val missingModifyAuthentications: MissingAuthentications
		get() = missingAuthentications(modifyAcl)
	override val missingResetAuthentications: MissingAuthentications
		get() = missingAuthentications(resetAcl)

	private val channel: CardChannel
		get() = application.channel

	private val passwordRef = did.parameters.passwordRef
	private val globalRef = did.scope == DidScope.GLOBAL
	private val passwordAttributes: PasswordAttributes = did.parameters.toSmartcardPasswordAttributes()
	private val unblockingAttributes: PasswordAttributes? =
		did.parameters.unblockingParameters?.toSmartcardPasswordAttributes()

	override val supportedModifyModes: Set<PinDid.ModifyMode> by lazy {
		did.parameters.pwdFlags.toModifyModes()
	}
	override val supportsModifyWithOldPassword: Boolean by lazy {
		PasswordFlags.MODIFY_WITH_OLD_PASSWORD in did.parameters.pwdFlags
	}
	override val supportsModifyWithoutOldPassword: Boolean by lazy {
		PasswordFlags.MODIFY_WITHOUT_OLD_PASSWORD in did.parameters.pwdFlags
	}

	override val supportedResetModes: Set<PinDid.ResetMode> by lazy {
		did.parameters.pwdFlags.toResetModes()
	}
	override val supportsResetWithoutData: Boolean by lazy {
		PasswordFlags.RESET_RETRY_COUNTER_WITHOUT_DATA in
			did.parameters.pwdFlags
	}
	override val supportsResetWithPassword: Boolean by lazy {
		PasswordFlags.RESET_RETRY_COUNTER_WITH_PASSWORD in
			did.parameters.pwdFlags
	}
	override val supportsResetWithUnblocking: Boolean by lazy {
		PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK in
			did.parameters.pwdFlags
	}
	override val supportsResetWithUnblockingAndPassword: Boolean by lazy {
		PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK_AND_PASSWORD in
			did.parameters.pwdFlags
	}

	private val hardwareVerify: VerifyPinFeature? by lazy {
		mapSmartcardError {
			channel.card.terminalConnection.feature<VerifyPinFeature>()
		}
	}

	override fun verifyPasswordInHardware(): Boolean = hardwareVerify != null

	private val hardwareModify: ModifyPinFeature? by lazy {
		mapSmartcardError {
			channel.card.terminalConnection.feature<ModifyPinFeature>()
		}
	}

	override fun modifyPasswordInHardware(): Boolean = hardwareModify != null

	override fun passwordStatus(): PinStatus =
		mapSmartcardError {
			val resp = Verify.verifyStatus(passwordRef, globalRef).transmit(channel)
			resp.toPinStatusOrThrow()
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun verify(password: String) =
		mapSmartcardError {
			throwIf(!missingAuthAuthentications.isSolved) { MissingAuthentication("Authenticate ACL is not satisfied") }

			val encPin = PinUtils.encodePin(password, passwordAttributes)
			val resp = Verify.verifyPlain(encPin, passwordRef, globalRef).transmit(channel)
			if (resp is SecurityCommandFailure) {
				throw PinCommandError(resp, "Failed to verify password ${did.name}")
			}
			setDidFulfilled()
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun verify(lang: UsbLangId) =
		mapSmartcardError {
			throwIf(!missingAuthAuthentications.isSolved) { MissingAuthentication("Authenticate ACL is not satisfied") }

// 		val feat = checkNotNull(hardwareVerify) { "Verify for hardware readers called without having support in the reader" }
//
// 		val dummyPin =
// 			throwIfNull(PinUtils.createPinMask(passwordAttributes)) {
// 				UnsupportedFeature("Unpadded passwords are not supported with hardware readers")
// 			}
// 		val cmdTemplate = Verify.verifyPlainTemplate(dummyPin, passwordRef, globalRef)
// 		val req = PinVerify.fromParams(passwordAttributes, cmdTemplate, lang = lang)
//
// 		feat.verifyPin(req)

			TODO("Not yet implemented")
		}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun modify(
		newPassword: String,
		oldPassword: String?,
	) = mapSmartcardError {
		throwIf(!missingModifyAuthentications.isSolved) { MissingAuthentication("Modify ACL is not satisfied") }

		val encPinNew = PinUtils.encodePin(newPassword, passwordAttributes)
		val encPinOld = oldPassword?.let { PinUtils.encodePin(it, passwordAttributes) }

		val req =
			if (encPinOld != null) {
				ChangeReferenceData.changeOldToNew(encPinOld, encPinNew, passwordRef, globalRef)
			} else {
				ChangeReferenceData.changeToNew(encPinNew, passwordRef, globalRef)
			}

		val resp = req.transmit(channel)
		if (resp is SecurityCommandFailure) {
			throw PinCommandError(resp, "Failed to modify password ${did.name}")
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override suspend fun modify(
		modifyMode: PinDid.ModifyMode,
		lang: UsbLangId,
	) = mapSmartcardError {
		throwIf(!missingModifyAuthentications.isSolved) { MissingAuthentication("Modify ACL is not satisfied") }

// 		val dummyPin =
// 			throwIfNull(PinUtils.createPinMask(passwordAttributes)) {
// 				UnsupportedFeature("Unpadded passwords are not supported with hardware readers")
// 			}
//
// 		// one PIN operations are performed with verify
// 		if (PasswordFlags.MODIFY_NEEDS_OLD_PASSWORD in did.parameters.pwdFlags) {
// 			val feat = checkNotNull(hardwareModify) { "Modify for hardware readers called without having support in the reader" }
//
// 			val cmdTemplate = ChangeReferenceData.changeOldToNewTemplate(dummyPin, passwordRef, globalRef)
// 			val req = PinModify.fromParams(passwordAttributes, cmdTemplate, lang = lang)
//
// 			// TODO: make feature call cancellable
// 			feat.modifyPin(req)
// 		} else {
// 			val feat = checkNotNull(hardwareVerify) { "Verify for hardware readers called without having support in the reader" }
//
// 			val cmdTemplate =
// 				if (PasswordFlags.MODIFY_WITH_RESET_RETRY_COUNTER in did.parameters.pwdFlags) {
// 					ResetRetryCounter.resetWithNewDataTemplate(dummyPin, passwordRef, globalRef)
// 				} else {
// 					ChangeReferenceData.changeToNewTemplate(dummyPin, passwordRef, globalRef)
// 				}
// 			val req = PinVerify.fromParams(passwordAttributes, cmdTemplate, lang = lang)
//
// 			// TODO: make feature call cancellable
// 			feat.verifyPin(req)
// 		}

		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun resetPassword(
		unblockingCode: String?,
		newPassword: String?,
	) = mapSmartcardError {
		throwIf(!missingResetAuthentications.isSolved) { MissingAuthentication("Reset ACL is not satisfied") }

		val encPinNew = newPassword?.let { PinUtils.encodePin(it, passwordAttributes) }

		val req =
			if (unblockingCode != null && encPinNew != null) {
				checkNotNull(unblockingAttributes) { "Unblocking password attributes must be defined" }
				val encUnblockingCode = PinUtils.encodePin(unblockingCode, unblockingAttributes)
				ResetRetryCounter.resetWithCodeAndNewData(encUnblockingCode, encPinNew, passwordRef, globalRef)
			} else if (encPinNew != null) {
				ResetRetryCounter.resetWithNewData(encPinNew, passwordRef, globalRef)
			} else if (unblockingCode != null) {
				checkNotNull(unblockingAttributes) { "Unblocking password attributes must be defined" }
				val encUnblockingCode = PinUtils.encodePin(unblockingCode, unblockingAttributes)
				ResetRetryCounter.resetWithCode(encUnblockingCode, passwordRef, globalRef)
			} else {
				ResetRetryCounter.resetNoData(passwordRef, globalRef)
			}

		val resp = req.transmit(channel)
		if (resp is SecurityCommandFailure) {
			throw PinCommandError(resp, "Failed to reset password ${did.name}")
		}
	}

	override fun resetPassword(
		resetMode: PinDid.ResetMode,
		lang: UsbLangId,
	) = mapSmartcardError {
		throwIf(!missingResetAuthentications.isSolved) { MissingAuthentication("Reset ACL is not satisfied") }

		TODO("Not yet implemented")
	}
}

internal fun PasswordEncodingDefinition.toSmartcardPasswordAttributes(): PasswordAttributes =
	PasswordAttributes(
		this.pwdType.toSmartcardPasswordType(),
		this.minLength,
		this.storedLength,
		this.maxLength,
		this.padChar,
	)

internal fun PasswordType.toSmartcardPasswordType(): org.openecard.sc.iface.feature.PasswordType =
	when (this) {
		PasswordType.BCD -> org.openecard.sc.iface.feature.PasswordType.BCD
		PasswordType.ISO_9564_1 -> org.openecard.sc.iface.feature.PasswordType.ISO_9564_1
		PasswordType.ASCII_NUMERIC -> org.openecard.sc.iface.feature.PasswordType.ASCII_NUMERIC
		PasswordType.UTF_8 -> org.openecard.sc.iface.feature.PasswordType.UTF_8
		PasswordType.HALF_NIBBLE_BCD -> org.openecard.sc.iface.feature.PasswordType.HALF_NIBBLE_BCD
	}

internal fun PasswordFlags.toModifyMode(): PinDid.ModifyMode? =
	when (this) {
		PasswordFlags.MODIFY_WITH_OLD_PASSWORD -> PinDid.ModifyMode.WITH_OLD_PASSWORD
		PasswordFlags.MODIFY_WITHOUT_OLD_PASSWORD -> PinDid.ModifyMode.WITHOUT_OLD_PASSWORD
		else -> null
	}

internal fun Set<PasswordFlags>.toModifyModes(): Set<PinDid.ModifyMode> = mapNotNull { it.toModifyMode() }.toSet()

internal fun PasswordFlags.toResetMode(): PinDid.ResetMode? =
	when (this) {
		PasswordFlags.RESET_RETRY_COUNTER_WITHOUT_DATA -> PinDid.ResetMode.WITHOUT_DATA
		PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK_AND_PASSWORD -> PinDid.ResetMode.WITH_UNBLOCK_AND_PASSWORD
		PasswordFlags.RESET_RETRY_COUNTER_WITH_PASSWORD -> PinDid.ResetMode.WITH_PASSWORD
		PasswordFlags.RESET_RETRY_COUNTER_WITH_UNBLOCK -> PinDid.ResetMode.WITH_UNBLOCK
		else -> null
	}

internal fun Set<PasswordFlags>.toResetModes(): Set<PinDid.ResetMode> = mapNotNull { it.toResetMode() }.toSet()
