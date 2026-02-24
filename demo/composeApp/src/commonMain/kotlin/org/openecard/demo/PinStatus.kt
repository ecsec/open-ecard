package org.openecard.demo

enum class PinStatus {
	OK,
	Retry,
	Suspended,
	Blocked,
	Unknown,
	WrongPIN,
	WrongCAN,
	WrongPUK,
}

data class PinOperationResult(
	val status: PinStatus?,
	val errorMessage: String? = null,
)
