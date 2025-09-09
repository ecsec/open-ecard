package org.openecard.sc.pace.crypto

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.pace.asn1.StandardizedDomainParameters
import org.openecard.sc.pace.crypto.BcDomainParameterResolver.resolveDomainParameters

private val log = KotlinLogging.logger { }

internal class JvmEacCryptoUtils : EacCryptoUtils {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun compressKey(
		keyData: UByteArray,
		domainParams: StandardizedDomainParameters,
	): UByteArray {
		val pk = BcEcKeyPair.decodePublicKey(domainParams.resolveDomainParameters(), keyData)
		return pk.encodedCompressed
	}
}
