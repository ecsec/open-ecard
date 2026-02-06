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
