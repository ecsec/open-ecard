package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu

data class GetChallenge(
	val algorithm: UByte,
	val le: UShort = 0u,
	val extendedLength: Boolean = false,
) : IsoCommandApdu,
	SecurityCommandApdu {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		CommandApdu(0x00u, 0x84u, algorithm, 0u, le = le, forceExtendedLength = extendedLength)
	}

	companion object {
		fun forAlgorithm(algorithm: UByte): GetChallenge = GetChallenge(algorithm)
	}
}
