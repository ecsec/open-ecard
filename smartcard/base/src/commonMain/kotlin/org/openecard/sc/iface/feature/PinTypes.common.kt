package org.openecard.sc.iface.feature

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

data class PasswordAttributes(
	val pwdFlags: Set<PasswordFlags>,
	val pwdType: PasswordType,
	val minLength: Int,
	val storedLength: Int,
	val maxLength: Int,
	val padChar: UByte?,
) {
	companion object {
		fun iso9564(
			minLength: Int,
			storedLength: Int,
			maxLength: Int,
		): PasswordAttributes =
			PasswordAttributes(
				setOf(PasswordFlags.NEEDS_PADDING),
				PasswordType.ISO_9564_1,
				minLength,
				storedLength,
				maxLength,
				0xFFu,
			)
	}

	val isIsoPin = pwdType == PasswordType.ISO_9564_1
}

enum class PasswordType {
	BCD,
	ISO_9564_1,
	ASCII_NUMERIC,
	UTF_8,
	HALF_NIBBLE_BCD,
}
