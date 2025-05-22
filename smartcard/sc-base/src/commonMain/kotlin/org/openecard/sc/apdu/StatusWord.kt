package org.openecard.sc.apdu

enum class StatusWord(
	val code: UShort,
	val description: String,
	val parameterMask: UShort? = null,
) {
	UNKNOWN_SW(0x0000u, "Status code not covered in ISO 7816-4", parameterMask = 0xFFFFu),

	OK(0x9000u, "No further qualification"),

	/**
	 * If SW1 is set to '61', then the process is completed and before issuing any other command, a GET RESPONSE
	 * command may be issued with the same CLA and using SW2 (number of data bytes still available) as short Le field.
	 */
	MORE_BYTES_AVAILABLE(0x6100u, "SW2 encodes the number of data bytes still available", parameterMask = 0x00FFu),

	NVMEM_UNCHANGED_WARN(
		0x6200u,
		"State of non-volatile memory is unchanged (further qualification in SW2)",
		parameterMask = 0x00FFu,
	),
	NVMEM_CHANGED_WARN(
		0x6300u,
		"State of non-volatile memory has changed (further qualification in SW2)",
		parameterMask = 0x00FFu,
	),

	NVMEM_UNCHANGED_ERROR(
		0x6400u,
		"State of non-volatile memory is unchanged (further qualification in SW2)",
		parameterMask = 0x00FFu,
	),
	NVMEM_CHANGED_ERROR(
		0x6500u,
		"State of non-volatile memory has changed (further qualification in SW2)",
		parameterMask = 0x00FFu,
	),

	// TODO: maybe split up when encountering more in commands
	SECURITY(0x6600u, "Security-related issue", parameterMask = 0x00FFu),
	WRONG_LENGTH_UNSPECIFIED(0x6700u, "Wrong length; no further indication"),
	UNSUPPORTED_CLA_FUNCTION(
		0x6800u,
		"Functions in CLA not supported (further qualification in SW2)",
		parameterMask = 0x00FFu,
	),
	COMMAND_NOT_ALLOWED(0x6900u, "Command not allowed (further qualification in SW2)", parameterMask = 0x00FFu),
	WRONG_COMMAND_PARAMS(0x6A00u, "Wrong parameters P1-P2 (further qualification in SW2)", parameterMask = 0x00FFu),
	WRONG_COMMAND_PARAMS_UNSPECIFIED(0x6B00u, "Wrong parameters P1-P2"),

	/**
	 * If SW1 is set to '6C', then the process is aborted and before issuing any other command, the same
	 * command may be re-issued using SW2 (exact number of available data bytes) as short Le field.
	 */
	WRONG_LE(0x6C00u, "Wrong Le field; SW2 encodes the exact number of available data bytes", parameterMask = 0x00FFu),
	INS_INVALID(0x6D00u, "Instruction code not supported or invalid"),
	CLASS_NOT_SUPPORTED(0x6E00u, "Class not supported"),
	NO_PRECISE_DIAGNOSIS(0x6F00u, "No precise diagnosis"),

	// SW specifications
	RETURN_DATA_CORRUPT(0x6281u, "Part of returned data may be corrupted"),
	EOF_REACHED_BEFORE_READING_NE(0x6282u, "End of file or record reached before reading Ne bytes"),
	SELECT_FILE_DEACTIVATED(0x6283u, "Selected file deactivated"),
	FCI_FORMATTING_INCORRECT(0x6284u, "File control information not formatted according to 5.3.3"),
	FILE_IN_TERMINATION_STATE(0x6285u, "Selected file in termination state"),
	NO_SENSOR_INPUT_AVAILABLE(0x6286u, "No input data available from a sensor on the card"),

	FILE_FULL(0x6381u, "File filled up by the last write"),
	COUNTER_ENCODED(
		0x63C0u,
		"Counter from 0 to 15 encoded by 'X' (exact meaning depending on the command)",
		parameterMask = 0x000Fu,
	),

	IMMEDIATE_CARD_RESPONSE_REQUIRED(0x6401u, "Immediate response required by the card"),

	MEMORY_FAILURE(0x6581u, "Memory failure"),

	LOGICAL_CHANNEL_UNSUPPORTED(0x6881u, "Logical channel not supported"),
	SECURE_MESSAGING_UNSUPPORTED(0x6882u, "Secure messaging not supported"),
	LAST_OF_CHAIN_EXPECTED(0x6883u, "Last command of the chain expected"),
	CHAINING_UNSUPPORTED(0x6884u, "Command chaining not supported"),

	COMMAND_INCOMPATIBLE_WITH_STRUCTURE(0x6981u, "Command incompatible with file structure"),
	SECURITY_STATUS_UNSATISFIED(0x6982u, "Security status not satisfied"),
	AUTH_BLOCKED(0x6983u, "Authentication method blocked"),
	REFERENCE_DATA_UNUSABLE(0x6984u, "Reference data not usable"),
	CONDITIONS_OF_USE_UNSATISFIED(0x6985u, "Conditions of use not satisfied"),
	COMMAND_FORBIDDEN(0x6986u, "Command not allowed (no current EF)"),
	SM_DO_MISSING(0x6987u, "Expected secure messaging data objects missing"),
	SM_DO_INCORRECT(0x6988u, "Incorrect secure messaging data objects"),

	WRONG_COMMAND_PARAMS_IN_DATA_FIELD(0x6A80u, "Incorrect parameters in the command data field"),
	FUNTION_UNSUPPORTED(0x6A81u, "Function not supported"),
	FILE_OR_APP_NOT_FOUND(0x6A82u, "File or application not found"),
	RECORD_NOT_FOUND(0x6A83u, "Record not found"),
	NO_SPACE_IN_FILE(0x6A84u, "Not enough memory space in the file"),
	NC_DOES_NOT_MATCH_TLV(0x6A85u, "Nc inconsistent with TLV structure"),
	P1_P2_INCORRECT(0x6A86u, "Incorrect parameters P1-P2"),
	NC_DOES_NOT_MATCH_P1_P2(0x6A87u, "Nc inconsistent with parameters P1-P2"),
	REFERENCED_DATA_NOT_FOUND(
		0x6A88u,
		"Referenced data or reference data not found (exact meaning depending on the command)",
	),
	FILE_ALREADY_EXISTS(0x6A89u, "File already exists"),
	DF_ALREADY_EXISTS(0x6A8Au, "DF name already exists"),
	;

	val isNormal by lazy { isNormal(code) }
	val isWarning by lazy { isWarning(code) }
	val isExecutionError by lazy { isExecutionError(code) }
	val isCheckingError by lazy { isCheckingError(code) }

	internal fun matches(sw: UShort): Boolean {
		val mask = (this.parameterMask ?: 0x0u).inv()
		return (mask and sw) == code
	}

	val parentCode by lazy {
		val remaining = sortedEntries.dropWhile { it != this }.drop(1)
		val result = remaining.find { it.matches(this.code) }
		if (result != UNKNOWN_SW) {
			result
		} else {
			null
		}
	}

	companion object {
		private val sortedEntries = entries.sortedBy { it.parameterMask ?: 0x0u }

		fun forSw(sw: UShort): StatusWord =
			sortedEntries.find {
				it.matches(sw)
			} ?: throw IllegalStateException("Uncovered status found.")
	}
}

data class StatusWordResult(
	val sw: UShort,
	val type: StatusWord,
	val parameter: UByte?,
) {
	val isNormal by lazy { isNormal(sw) }
	val isWarning by lazy { isWarning(sw) }
	val isExecutionError by lazy { isExecutionError(sw) }
	val isCheckingError by lazy { isCheckingError(sw) }
}

fun UShort.toStatusWord(): StatusWordResult {
	val status = StatusWord.forSw(this)
	val param =
		status.parameterMask?.let {
			if (it < 0x0Fu) {
				(it and this).toUByte()
			} else {
				null
			}
		}
	return StatusWordResult(this, status, param)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun isNormal(sw: UShort): Boolean = matches(sw, 0x9000u) || matches(mask = 0xFF00u, sw, 0x6100u)

@OptIn(ExperimentalUnsignedTypes::class)
private fun isWarning(sw: UShort): Boolean = 0x6200u <= sw && sw <= 0x63FFu

@OptIn(ExperimentalUnsignedTypes::class)
private fun isExecutionError(sw: UShort): Boolean = 0x6400u <= sw && sw <= 0x66FFu

@OptIn(ExperimentalUnsignedTypes::class)
private fun isCheckingError(sw: UShort): Boolean = 0x6700u <= sw && sw <= 0x6FFFu

@OptIn(ExperimentalUnsignedTypes::class)
private fun matches(
	mask: UShort = 0xFFFFu,
	code: UShort,
	vararg values: UShort,
): Boolean = (code and mask) in values
