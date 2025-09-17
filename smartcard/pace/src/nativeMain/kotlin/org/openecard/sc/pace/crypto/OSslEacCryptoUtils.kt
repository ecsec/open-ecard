package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.StandardizedDomainParameters

internal class OSslEacCryptoUtils : EacCryptoUtils {
	override fun compressKey(
		keyData: UByteArray,
		domainParams: StandardizedDomainParameters,
	) = OSslEcCurve(domainParams).use { curve ->
		OSslEcPublicKey.decodePublicKey(keyData, curve).encodedCompressed
	}
}
