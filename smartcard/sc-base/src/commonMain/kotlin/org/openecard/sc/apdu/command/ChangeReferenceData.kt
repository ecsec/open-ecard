package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

data class ChangeReferenceData(
	val p1: ChangeReferenceDataP1,
	val p2: SecurityCommandP2,
	val verificationData: PrintableUByteArray,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		CommandApdu(0x00u, 0x24u, p1.code, p2.byte, verificationData)
	}

	enum class ChangeReferenceDataP1(
		val code: UByte,
	) {
		OLD_AND_NEW_DATA(0u),
		NEW_DATA(1u),
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun changeOldToNew(
			oldData: UByteArray,
			newData: UByteArray,
			securityReference: UByte,
			globalReference: Boolean = true,
		): ChangeReferenceData =
			ChangeReferenceData(
				ChangeReferenceDataP1.OLD_AND_NEW_DATA,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				(oldData + newData).toPrintable(),
			)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun changeToNew(
			newData: UByteArray,
			securityReference: UByte,
			globalReference: Boolean = true,
		): ChangeReferenceData =
			ChangeReferenceData(
				ChangeReferenceDataP1.NEW_DATA,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				newData.toPrintable(),
			)
	}
}
