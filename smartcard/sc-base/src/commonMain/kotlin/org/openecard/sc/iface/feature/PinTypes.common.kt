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

class PinError(
	val error: PinErrorType,
	val pinStatus: PinStatus? = null,
	msg: String? = null,
	cause: Throwable? = null,
) : Exception(
		msg ?: "Error in PIN verification/modification.",
		cause,
	)

class PinStatus(
	val numRetries: Int,
)

enum class PinErrorType(
	val code: UShort,
	val mask: UShort = 0xFFFFu,
) {
	PIN_WRONG_NO_INFO(0x6300u),
	PIN_WRONG(0x63C0u, 0xFFF0u),
	REFERENCE_DATA_NOT_FOUND(0x6A88u),
	// TODO: add more codes
	;

	fun matchesCode(code: UShort): Boolean = this.code == (code and this.mask)

	companion object {
		fun findForCode(code: UShort): PinErrorType? = entries.find { it.matchesCode(code) }
	}
}

fun UShort.toPinError(): PinError? {
	val errorType = PinErrorType.findForCode(this)
	return errorType?.let {
		val status =
			when (errorType) {
				PinErrorType.PIN_WRONG -> PinStatus((this and 0xFu).toInt())
				else -> null
			}
		PinError(errorType, status)
	}
}
