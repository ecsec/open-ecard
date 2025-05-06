package org.openecard.sc.iface

sealed interface Feature

interface PaceFeature : Feature

interface ModifyPinFeature : Feature

interface VerifyPinFeature : Feature {
	fun verifyPin(
		passwordAttributes: PasswordAttributes,
		template: ByteArray,
	): ResponseApdu
}

data class PasswordAttributes(
	val pwdFlags: Set<PasswordFlags>,
	val pwdType: PasswordType,
	val minLength: Int,
	val storedLength: Int,
	val maxLength: Int,
	val padChar: UByte,
)

// TODO: check what makes sense here and remove unnecessary elements
enum class PasswordFlags {
	CASE_SENSITIVE,
	LOCAL,
	CHANGE_DISABLED,
	UNBLOCK_DISABLED,
	INITIALIZED,
	NEEDS_PADDING,
	UNBLOCKING_PASSWORD,
	SO_PASSWORD,
	DISABLE_ALLOWED,
	INTEGRITY_PROTECTED,
	CONFIDENTIALITY_PROTECTED,
	EXCHANGE_REF_DATA,
	RESET_RETRY_COUNTER_1,
	RESET_RETRY_COUNTER_2,
}

enum class PasswordType {
	BCD,
	ISO_9564_1,
	ASCII_NUMERIC,
	UTF_8,
	HALF_NIBBLE_BCD,
}
