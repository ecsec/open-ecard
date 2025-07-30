package org.openecard.addons.tr03124.eac

import org.openecard.sc.pace.cvc.CvcChain

interface TerminalAuthentication {
	@OptIn(ExperimentalUnsignedTypes::class)
	val challenge: UByteArray

	fun verifyCertificates(chain: CvcChain)

	fun verifyTerminalSignature()
}
