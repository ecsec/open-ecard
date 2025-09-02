package org.openecard.addons.tr03124.eac

import org.openecard.sc.pace.cvc.CardVerifiableCertificate
import org.openecard.sc.pace.cvc.CvcChain
import org.openecard.sc.tlv.Tlv

interface TerminalAuthentication {
	@OptIn(ExperimentalUnsignedTypes::class)
	val challenge: UByteArray

	fun verifyCertificates(chain: CvcChain)

	@OptIn(ExperimentalUnsignedTypes::class)
	fun verifyTerminalSignature(
		terminalCertificate: CardVerifiableCertificate,
		terminalSignature: UByteArray,
		pcdKey: UByteArray,
		aad: Tlv?,
	)
}
