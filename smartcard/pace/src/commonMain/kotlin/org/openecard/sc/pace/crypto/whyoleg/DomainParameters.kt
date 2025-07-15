package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.algorithms.EC
import org.openecard.sc.pace.asn1.StandardizedDomainParameters

fun StandardizedDomainParameters.Curve.toKotlinCrypto(): EC.Curve =
	when (this) {
		StandardizedDomainParameters.Curve.Secp256r1 -> EC.Curve.P256
		StandardizedDomainParameters.Curve.BrainpoolP256r1 -> EC.Curve.brainpoolP256r1
		StandardizedDomainParameters.Curve.Secp384r1 -> EC.Curve.P384
		StandardizedDomainParameters.Curve.BrainpoolP384r1 -> EC.Curve.brainpoolP384r1
		StandardizedDomainParameters.Curve.BrainpoolP512r1 -> EC.Curve.brainpoolP512r1
		StandardizedDomainParameters.Curve.Secp521r1 -> EC.Curve.P521
		else -> throw UnsupportedOperationException("Unsupported curve $this")
	}
