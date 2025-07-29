package org.openecard.sc.iface.feature

import org.openecard.sc.apdu.ApduProcessingError
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandResult
import org.openecard.sc.apdu.command.SecurityCommandResultType
import org.openecard.sc.apdu.command.SecurityCommandSuccess

data class PasswordAttributes(
	val pwdType: PasswordType,
	val minLength: UInt,
	val storedLength: UInt?,
	val maxLength: UInt?,
	val padChar: UByte?,
) {
	companion object {
		fun iso9564(
			minLength: UInt,
			storedLength: UInt,
			maxLength: UInt,
		): PasswordAttributes =
			PasswordAttributes(
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

class PinCommandError(
	val error: SecurityCommandFailure,
	msg: String? = null,
) : Exception(
		msg ?: error.ex.message ?: "Error in PIN verification/modification.",
		error.ex,
	)

class PinStatus(
	val numRetries: Int?,
	val status: SecurityCommandResultType,
)

@Throws(ApduProcessingError::class)
fun SecurityCommandResult.toPinStatusOrThrow(): PinStatus =
	when (this) {
		is SecurityCommandFailure ->
			when (this.resultType) {
				SecurityCommandResultType.AUTH_DEACTIVATED,
				SecurityCommandResultType.AUTH_BLOCKED,
				SecurityCommandResultType.COUNTER,
				-> PinStatus(this.retries, this.resultType)
				SecurityCommandResultType.VERIFICATION_FAILED,
				SecurityCommandResultType.CONDITION_NOT_SATISFIED,
				SecurityCommandResultType.SEC_STATUS_NOT_SATISFIED,
				SecurityCommandResultType.UNKNOWN_REFERENCE,
				SecurityCommandResultType.OTHER_ERROR,
				// this is an error and not a successfully retrieved pin status
				-> throw this.ex
				SecurityCommandResultType.OK,
				// this is captured in the other type
				-> throw IllegalStateException("Logic error in PinState creation")
			}
		is SecurityCommandSuccess -> PinStatus(null, this.resultType)
	}
