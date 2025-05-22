package org.openecard.sc.apdu

class ChainedCommandApdu(
	val original: CommandApdu,
	val head: List<CommandApdu>,
	val tail: CommandApdu,
)

@OptIn(ExperimentalUnsignedTypes::class)
fun CommandApdu.toChained(): ChainedCommandApdu {
	val cla =
		checkNotNull(this.classByteInterIndustry) { "Can not build a chained APDU without an interindustry class byte." }
	require(!cla.commandChaining) { "Can not convert a chained APDU to a chained APDU." }

	val chunks = this.data.chunked(0xFF)
	val head =
		chunks.take(chunks.size - 1).map {
			CommandApdu(
				cla.setCommandChaining(true).byte,
				ins,
				p1,
				p2,
				it.toUByteArray(),
				le,
				false,
			)
		}
	val tail =
		CommandApdu(
			cla.setCommandChaining(false).byte,
			ins,
			p1,
			p2,
			chunks.last().toUByteArray(),
			le,
			false,
		)

	return ChainedCommandApdu(this, head, tail)
}
