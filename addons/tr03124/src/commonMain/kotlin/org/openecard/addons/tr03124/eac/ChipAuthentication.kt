package org.openecard.addons.tr03124.eac

interface ChipAuthentication {
	fun authenticate(): ChipAuthenticationResult

	class ChipAuthenticationResult
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val efCardSecurity: UByteArray,
			val nonce: UByteArray,
			val token: UByteArray,
		)
}
