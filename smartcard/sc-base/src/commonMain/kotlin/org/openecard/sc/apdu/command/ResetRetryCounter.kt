package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

class ResetRetryCounter(
	val p1: ResetRetryCounterP1,
	val p2: SecurityCommandP2,
	val verificationData: PrintableUByteArray?,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		CommandApdu(0x00u, 0x2Cu, p1.code, p2.byte, verificationData ?: UByteArray(0).toPrintable())
	}

	enum class ResetRetryCounterP1(
		val code: UByte,
	) {
		RESET_CODE_AND_NEW_DATA(0u),
		RESET_CODE(1u),
		NEW_DATA(2u),
		NO_DATA(3u),
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun resetWithCodeAndNewData(
			resetCode: UByteArray,
			newReferenceData: UByteArray,
			securityReference: UByte,
			globalReference: Boolean = true,
		): ResetRetryCounter =
			ResetRetryCounter(
				ResetRetryCounterP1.RESET_CODE_AND_NEW_DATA,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				(resetCode + newReferenceData).toPrintable(),
			)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun resetWithCode(
			resetCode: UByteArray,
			securityReference: UByte,
			globalReference: Boolean = true,
		): ResetRetryCounter =
			ResetRetryCounter(
				ResetRetryCounterP1.RESET_CODE,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				resetCode.toPrintable(),
			)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun resetWithNewData(
			newReferenceData: UByteArray,
			securityReference: UByte,
			globalReference: Boolean = true,
		): ResetRetryCounter =
			ResetRetryCounter(
				ResetRetryCounterP1.NEW_DATA,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				newReferenceData.toPrintable(),
			)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun resetNoData(
			securityReference: UByte,
			globalReference: Boolean = true,
		): ResetRetryCounter =
			ResetRetryCounter(
				ResetRetryCounterP1.NO_DATA,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				null,
			)
	}
}
