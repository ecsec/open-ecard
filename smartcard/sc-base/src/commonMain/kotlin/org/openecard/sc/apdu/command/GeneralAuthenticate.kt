package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.InterIndustryClassByte
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.buildTlv
import org.openecard.utils.serialization.toPrintable

data class GeneralAuthenticate(
	val algorithm: UByte,
	val p2: SecurityCommandP2,
	val data: List<Tlv>,
	val le: UShort? = null,
	val extendedLength: Boolean = false,
	val ins: UByte = 0x86u,
	val cla: InterIndustryClassByte = InterIndustryClassByte.Default,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		val dataBytes =
			buildTlv(Tag.forTagNumWithClass(0x7Cu)) {
				data.forEach { generic(it) }
			}.toBer()
		CommandApdu(
			cla.byte,
			ins,
			algorithm,
			p2.byte,
			dataBytes.toPrintable(),
			le,
			forceExtendedLength = extendedLength,
		)
	}

	fun setCommandChaining(isChaining: Boolean = true) = copy(cla = cla.setCommandChaining(isChaining))

	companion object {
		fun withData(
			data: List<Tlv> = listOf(),
			le: UShort = 0u,
			algorithm: UByte = 0u,
			p2: SecurityCommandP2 = SecurityCommandP2.Default,
			extendedLength: Boolean = false,
		): GeneralAuthenticate = GeneralAuthenticate(algorithm, p2, data, le, extendedLength)

		fun withoutData(
			data: List<Tlv> = listOf(),
			algorithm: UByte = 0u,
			p2: SecurityCommandP2 = SecurityCommandP2.Default,
		): GeneralAuthenticate = GeneralAuthenticate(algorithm, p2, data, null)
	}
}

class SecurityCommandP2(
	val globalReference: Boolean,
	val qualifier: UByte,
	val reserved: UByte = 0u,
) {
	val byte: UByte by lazy {
		val b1 = if (globalReference) 0u else 1u
		val b2 = reserved.toUInt() and 0x11u
		val b3 = qualifier.toUInt() and 0xF1u
		val result = (b1 shl 7) or (b2 shl 5) or b3
		result.toUByte()
	}

	companion object {
		val Default = SecurityCommandP2(true, 0x0u, 0x0u)

		fun forQualifier(
			qualifier: UByte,
			globalReference: Boolean = true,
		): SecurityCommandP2 = SecurityCommandP2(globalReference, qualifier)
	}
}
