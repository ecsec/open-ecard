package org.openecard.sc.iface.feature

// TODO: check what makes sense here and remove unnecessary elements
enum class PasswordFlags {
	/**
	 * meaning that a user-given password shall not be converted to all-uppercase before presented to the card
	 */
	CASE_SENSITIVE,

	/**
	 * meaning that the password is local to the application to which it belongs
	 */
	LOCAL,

	/**
	 * meaning that it is not possible to change the password
	 */
	CHANGE_DISABLED,

	/**
	 * meaning that it is not possible to unblock the password
	 */
	UNBLOCK_DISABLED,

	/**
	 * meaning that, depending on the length of the given password and the stored length, the password may need to be
	 * padded before being presented to the card
	 */
	NEEDS_PADDING,

	/**
	 * is an unblockingPassword (ISO/IEC 7816-4 resetting code), meaning that this password may be used for unblocking
	 * purposes, i.e. to reset the retry counter of the related authentication object to its initial value
	 */
	UNBLOCKING_PASSWORD,

	/**
	 * is a soPassword , meaning that the password is a Security Officer (administrator) password
	 */
	SO_PASSWORD,

	/**
	 * is disable-allowed , meaning that the password might be disabled
	 */
	DISABLE_ALLOWED,

	/**
	 * shall be presented to the card with secure messaging (integrity-protected)
	 */
	INTEGRITY_PROTECTED,

	/**
	 * shall be presented to the card encrypted (confidentiality-protected)
	 */
	CONFIDENTIALITY_PROTECTED,

	/**
	 * can be changed by just presenting new reference data to the card or if both old and new reference data needs to
	 * be presented. If the bit is set, both old and new reference data shall be presented;
	 * otherwise only new reference data needs to be presented (exchangeRefData)
	 */
	EXCHANGE_REF_DATA,

	// TODO: Clarify what these mean and if they are necessary, as they are missing in 7816
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
