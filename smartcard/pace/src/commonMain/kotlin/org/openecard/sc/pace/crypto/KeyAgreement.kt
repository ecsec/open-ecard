package org.openecard.sc.pace.crypto

interface KeyAgreement {
	fun derive(
		privKey: ByteArray,
		pubKey: ByteArray,
	): ByteArray
}

expect fun ecdhAgreement(curve: StandardizedDomainParameters.Curve): KeyAgreement
