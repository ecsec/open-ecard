package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.StandardizedDomainParameters

interface EacCryptoUtils {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun compressKey(
		keyData: UByteArray,
		domainParams: StandardizedDomainParameters,
	): UByteArray
}

expect fun eacCryptoUtils(): EacCryptoUtils
