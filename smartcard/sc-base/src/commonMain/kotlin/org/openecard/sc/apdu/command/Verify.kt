package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.mergeToArray
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

/**
 * Verify command.
 *
 * The command initiates the comparison in the card of stored reference data with verification data sent from the
 * interface device (e.g., password) or from a sensor on the card (e.g., fingerprint). The security status may be
 * modified as a result of a comparison. The card may record unsuccessful comparisons (e.g., to limit the number of
 * further uses of the reference data).
 */
data class Verify(
	val ins: VerifyIns,
	val p2: SecurityCommandP2,
	val data: PrintableUByteArray?,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		CommandApdu(0x00u, ins.code, 0u, p2.byte, (data ?: ubyteArrayOf().toPrintable()))
	}

	enum class VerifyIns(
		val code: UByte,
	) {
		/**
		 * INS = '20': the command data field is normally present for conveying verification data. The absence of
		 * command data field is used to check whether the verification is required (SW1-SW2 = '63CX' where 'X'
		 * encodes the number of further allowed retries), or not (SW1-SW2 = '9000').
		 */
		PLAIN(0x20u),

		/**
		 * INS = '21': the command data field shall convey a verification data object (e.g., tag '5F2E', see ISO/IEC
		 * 7816-11[4], normally not empty. The presence of an empty verification data object with an extended
		 * header list (tag '4D', see 8.5.1) expresses that the verification data come from a sensor on the card.
		 */
		DATA_OBJECT(0x21u),
	}

	companion object {
		fun verifyStatus(
			securityReference: UByte,
			globalReference: Boolean,
		) = Verify(VerifyIns.PLAIN, SecurityCommandP2.forQualifier(securityReference, globalReference), null)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyPlain(
			verificationData: UByteArray,
			securityReference: UByte,
			globalReference: Boolean,
		) = Verify(
			VerifyIns.PLAIN,
			SecurityCommandP2.forQualifier(securityReference, globalReference),
			verificationData.toPrintable(),
		)

		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyPlainTemplate(
			dummyPassword: UByteArray,
			securityReference: UByte,
			globalReference: Boolean,
		): UByteArray {
			val command =
				Verify(
					VerifyIns.PLAIN,
					SecurityCommandP2.forQualifier(securityReference, globalReference),
					dummyPassword.toPrintable(),
				)
			return command.apdu.toBytes
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		fun verifyBiometric(
			verificationDataObject: Tlv,
			securityReference: UByte,
			globalReference: Boolean,
			extendedHeaders: UByteArray? = null,
		): Verify {
			val data =
				buildList {
					add(verificationDataObject)
					// extended headers defined in sec.8.5.1
					if (extendedHeaders != null) {
						TlvPrimitive(Tag.forTagNumWithClass(0x4Du), extendedHeaders.toPrintable())
					}
				}.map { it.toBer() }.mergeToArray()

			return Verify(
				VerifyIns.DATA_OBJECT,
				SecurityCommandP2.forQualifier(securityReference, globalReference),
				data.toPrintable(),
			)
		}
	}
}
