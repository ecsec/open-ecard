package org.openecard.sc.apdu

import org.openecard.utils.common.cast
import org.openecard.utils.common.toUByte
import org.openecard.utils.common.toUShort
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

/**
 * Class representing an APDU.
 *
 * The following variants are possible:
 * ```
 * Case 1. : |CLA|INS|P1|P2|
 * Case 2. : |CLA|INS|P1|P2|LC|DATA|
 * Case 2.1: |CLA|INS|P1|P2|EXTLC|DATA|
 * Case 3. : |CLA|INS|P1|P2|LE|
 * Case 3.1: |CLA|INS|P1|P2|EXTLE|
 * Case 4. : |CLA|INS|P1|P2|LC|DATA|LE|
 * Case 4.1: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
 * ```
 */
data class CommandApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val cla: UByte,
		val ins: UByte,
		val p1: UByte,
		val p2: UByte,
		val data: PrintableUByteArray = ubyteArrayOf().toPrintable(),
		val le: UShort? = null,
		val forceExtendedLength: Boolean = false,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val lc: UShort = data.v.size.toUShort()

		val classByte: ClassByte by lazy { ClassByte.parse(cla) }
		val classByteInterIndustry: InterIndustryClassByte? by lazy { classByte.cast() }

		@OptIn(ExperimentalUnsignedTypes::class)
		val header: UByteArray by lazy { ubyteArrayOf(cla, ins, p1, p2) }

		@OptIn(ExperimentalUnsignedTypes::class)
		val toBytes: UByteArray by lazy {
			val isLcExtended = lc > 0xFFu
			val isLeExtended = le?.let { it > 0xFFu } == true
			val isExtended = forceExtendedLength || isLcExtended || isLeExtended

			val lcField =
				if (lc > 0u) {
					if (isExtended) {
						ubyteArrayOf(0x00u, (lc.rotateRight(8) and 0xFFu).toUByte(), (lc and 0xFFu).toUByte())
					} else {
						ubyteArrayOf((lc and 0xFFu).toUByte())
					}
				} else {
					ubyteArrayOf()
				}
			val leField =
				le?.let {
					if (isExtended) {
						if (lc > 0u) {
							ubyteArrayOf((le.rotateRight(8) and 0xFFu).toUByte(), (le and 0xFFu).toUByte())
						} else {
							ubyteArrayOf(0x00u, (le.rotateRight(8) and 0xFFu).toUByte(), (le and 0xFFu).toUByte())
						}
					} else {
						ubyteArrayOf((le and 0xFFu).toUByte())
					}
				} ?: ubyteArrayOf()

			// assemble apdu
			header + lcField + data.v + leField
		}
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toCommandApdu(): CommandApdu = toUByteArray().toCommandApdu()

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toCommandApdu(): CommandApdu {
	val cla = this[0]
	val ins = this[1]
	val p1 = this[2]
	val p2 = this[3]

	val body = this.sliceArray(4 until this.size)
	val data: UByteArray
	val le: UShort?
	val isExtended: Boolean

	if (body.isEmpty()) {
		// no data and no le
		data = ubyteArrayOf()
		le = null
		isExtended = false
	} else {
		val b1 = body[0]
		if (b1 == 0u.toUByte()) {
			// either LE or extended length
			if (body.size == 1) {
				// only LE present
				data = ubyteArrayOf()
				le = b1.toUShort()
				isExtended = false
			} else if (body.size == 3) {
				// only LE present
				data = ubyteArrayOf()
				le = body.toUShort(1)
				isExtended = true
			} else {
				// extended length LC field, the next two bytes after b1 are LC
				isExtended = true
				val lc = body.toUShort(1).toInt()
				data = body.sliceArray(3 until 3 + lc)

				val leField = body.sliceArray(3 + lc until body.size)
				le =
					if (leField.isEmpty()) {
						null
					} else {
						require(leField.size == 2) { "LE field is not extended" }
						leField.toUShort(0)
					}
			}
		} else {
			// not extended length
			isExtended = false

			if (body.size == 1) {
				// only LE present
				data = ubyteArrayOf()
				le = body.toUByte(0).toUShort()
			} else {
				val lc = body.toUByte(0).toInt()
				data = body.sliceArray(1 until 1 + lc)

				val leField = body.sliceArray(1 + lc until body.size)
				le =
					if (leField.isEmpty()) {
						null
					} else {
						require(leField.size == 1) { "LE field contains more than one byte" }
						leField.toUByte(0).toUShort()
					}
			}
		}
	}

	return CommandApdu(cla, ins, p1, p2, data.toPrintable(), le, isExtended)
}
