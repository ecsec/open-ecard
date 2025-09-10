package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.apdu.InterIndustryClassByte
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

data class ExternalAuthenticate
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val algorithm: UByte,
		val p2: SecurityCommandP2,
		val data: PrintableUByteArray?,
		val le: UShort? = null,
		val extendedLength: Boolean = false,
		val cla: InterIndustryClassByte = InterIndustryClassByte.Default,
	) : IsoCommandApdu,
		SecurityCommandApdu {
		@OptIn(ExperimentalUnsignedTypes::class)
		override val apdu: CommandApdu by lazy {
			CommandApdu(
				cla.byte,
				0x82u,
				algorithm,
				p2.byte,
				data ?: ubyteArrayOf().toPrintable(),
				le,
				forceExtendedLength = extendedLength,
			)
		}

		fun setCommandChaining(isChaining: Boolean = true) = copy(cla = cla.setCommandChaining(isChaining))

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun withData(
				data: UByteArray?,
				le: UShort = 0u,
				algorithm: UByte = 0u,
				p2: SecurityCommandP2 = SecurityCommandP2.Default,
				extendedLength: Boolean = false,
			): ExternalAuthenticate = ExternalAuthenticate(algorithm, p2, data?.toPrintable(), le, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun withoutData(
				data: UByteArray?,
				algorithm: UByte = 0u,
				p2: SecurityCommandP2 = SecurityCommandP2.Default,
			): ExternalAuthenticate = ExternalAuthenticate(algorithm, p2, data?.toPrintable(), null)
		}
	}
