package org.openecard.sc.pace.crypto

import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.jce.ECNamedCurveTable

object BcDomainParameterResolver {
	fun StandardizedDomainParameters.resolveDomainParameters(): ECDomainParameters {
		val curveName =
			when (this) {
				StandardizedDomainParameters.Curve.Secp192r1 -> "secp192r1"
				StandardizedDomainParameters.Curve.BrainpoolP192r1 -> "BrainpoolP192r1"
				StandardizedDomainParameters.Curve.Secp224r1 -> "secp224r1"
				StandardizedDomainParameters.Curve.BrainpoolP224r1 -> "BrainpoolP224r1"
				StandardizedDomainParameters.Curve.Secp256r1 -> "secp256r1"
				StandardizedDomainParameters.Curve.BrainpoolP256r1 -> "BrainpoolP256r1"
				StandardizedDomainParameters.Curve.BrainpoolP320r1 -> "BrainpoolP320r1"
				StandardizedDomainParameters.Curve.Secp384r1 -> "secp384r1"
				StandardizedDomainParameters.Curve.BrainpoolP384r1 -> "BrainpoolP384r1"
				StandardizedDomainParameters.Curve.BrainpoolP512r1 -> "BrainpoolP512r1"
				StandardizedDomainParameters.Curve.Secp521r1 -> "secp521r1"
			}
		val spec =
			ECNamedCurveTable.getParameterSpec(curveName)
				?: throw UnsupportedOperationException("Crypto engine does not support the requested curve $curveName")
		return ECDomainParameters(spec.curve, spec.g, spec.n, spec.h, spec.seed)
	}
}
