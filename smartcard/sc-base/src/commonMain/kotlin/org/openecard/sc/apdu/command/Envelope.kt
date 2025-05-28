package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.ClassByte
import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.toPrintable

class EnvelopeApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val chained: Boolean,
		val data: UByteArray,
		val le: UShort?,
		forceExtendedLength: Boolean = false,
	) : IsoCommandApdu {
		@OptIn(ExperimentalUnsignedTypes::class)
		override val apdu: CommandApdu by lazy {
			val cls = ClassByte.parseInterIndustry(0x00u).setCommandChaining(chained)
			CommandApdu(
				cls.byte,
				0xC2u,
				0x00u,
				0x00u,
				data = data.toPrintable(),
				le = le,
				forceExtendedLength = forceExtendedLength,
			)
		}
	}

class EnvelopeDo
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val chained: Boolean,
		val data: UByteArray,
	) : IsoCommandApdu {
		@OptIn(ExperimentalUnsignedTypes::class)
		override val apdu: CommandApdu by lazy {
			val cls = ClassByte.parseInterIndustry(0x00u).setCommandChaining(chained)
			CommandApdu(cls.byte, 0xC3u, 0x00u, 0x00u, data = data.toPrintable())
		}
	}
