package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.StandardizedDomainParameters

interface KeyAgreement {
	fun derive(
		privKey: ByteArray,
		pubKey: ByteArray,
	): ByteArray
}

expect fun ecdhAgreement(curve: StandardizedDomainParameters.Curve): KeyAgreement
