package org.openecard.sc.iface

import org.openecard.sc.utils.PrintableByteArray

sealed interface Feature

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

interface PaceFeature : Feature {
	val paceCapabilities: Set<PaceCapability>

	fun establishChannel(req: PaceEstablishChannelRequest): PaceEstablishChannelResponse =
		establishChannel(req.pinId.code, req.chat.v, req.pin.v)

	fun establishChannel(
		pinId: UByte,
		chat: ByteArray,
		pin: ByteArray,
	): PaceEstablishChannelResponse

	fun destroyChannel()
}

enum class PaceCapability(
	val code: UByte,
) {
	QES(0x10u),
	GERMAN_EID(0x20u),
	GENERIC_PACE(0x40u),
	DESTROY_CHANNEL(0x80u),
}

enum class PacePinId(
	val code: UByte,
) {
	MRZ(0x1u),
	CAN(0x2u),
	PIN(0x3u),
	PUK(0x4u),
}

data class PaceEstablishChannelRequest(
	val pinId: PacePinId,
	val chat: PrintableByteArray,
	val pin: PrintableByteArray,
)

data class PaceEstablishChannelResponse(
	val status: UShort,
	val efCardAccess: PrintableByteArray,
	val carCurr: PrintableByteArray,
	val carPrev: PrintableByteArray,
	val idIcc: PrintableByteArray,
)
