package org.openecard.sc.apdu.sm

sealed interface PaddingContentIndicator {
	val byte: UByte

	object NoIndication : PaddingContentIndicator {
		override val byte: UByte = 0u
	}

	object CbcPadding : PaddingContentIndicator {
		override val byte: UByte = 1u
	}

	object NoPadding : PaddingContentIndicator {
		override val byte: UByte = 2u
	}

	class DataEncryption(
		keyNibble: UByte,
	) : PaddingContentIndicator {
		override val byte: UByte = (0x10u or keyNibble.toUInt()).toUByte()
	}

	class KeyEncryption(
		keyNibble: UByte,
	) : PaddingContentIndicator {
		override val byte: UByte = (0x20u or keyNibble.toUInt()).toUByte()
	}

	class PrivateKeyReference(
		keyNibble: UByte,
	) : PaddingContentIndicator {
		override val byte: UByte = (0x30u or keyNibble.toUInt()).toUByte()
	}

	class Password(
		keyNibble: UByte,
	) : PaddingContentIndicator {
		override val byte: UByte = (0x40u or keyNibble.toUInt()).toUByte()
	}

	class Proprietary(
		override val byte: UByte,
	) : PaddingContentIndicator

	companion object {
		@OptIn(ExperimentalStdlibApi::class)
		fun fromIndicatorByte(byte: UByte): PaddingContentIndicator =
			if (byte.toUInt() == 0u) {
				NoIndication
			} else if (byte.toUInt() == 1u) {
				CbcPadding
			} else if (byte.toUInt() == 2u) {
				NoPadding
			} else if (byte >= 0x10u && byte <= 0x4Fu) {
				val keyNibble = (byte.toUInt() and 0xFu).toUByte()
				val upper = byte.toUInt() shr 4
				when (upper) {
					1u -> DataEncryption(keyNibble)
					2u -> KeyEncryption(keyNibble)
					3u -> PrivateKeyReference(keyNibble)
					4u -> Password(keyNibble)
					else -> {
						throw IllegalArgumentException("Content Indicator Byte contains and undefined value 0x${byte.toHexString()}")
					}
				}
			} else if (byte >= 0x80u && byte <= 0x8Eu) {
				Proprietary(byte)
			} else {
				throw IllegalArgumentException("Content Indicator Byte contains and undefined value 0x${byte.toHexString()}")
			}
	}
}
