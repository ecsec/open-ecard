package org.openecard.sc.pace.asn1

sealed interface StandardizedDomainParameters {
	enum class Curve : StandardizedDomainParameters {
		Secp192r1,
		BrainpoolP192r1,
		Secp224r1,
		BrainpoolP224r1,
		Secp256r1,
		BrainpoolP256r1,
		BrainpoolP320r1,
		Secp384r1,
		BrainpoolP384r1,
		BrainpoolP512r1,
		Secp521r1,
	}

	companion object {
		fun forParameterId(parameterId: UInt): StandardizedDomainParameters =
			// See BSI-TR-03110 version 2.05 section A.2.1.1.
			when (parameterId) {
				in 0u until 3u -> throw UnsupportedOperationException("DH groups are unsupported (parameterId=$parameterId)")
				8u -> Curve.Secp192r1
				9u -> Curve.BrainpoolP192r1
				10u -> Curve.Secp224r1
				11u -> Curve.BrainpoolP224r1
				12u -> Curve.Secp256r1
				13u -> Curve.BrainpoolP256r1
				14u -> Curve.BrainpoolP320r1
				15u -> Curve.Secp384r1
				16u -> Curve.BrainpoolP384r1
				17u -> Curve.BrainpoolP512r1
				18u -> Curve.Secp521r1
				else -> throw UnsupportedOperationException("Unsupported PACE domain parameters $parameterId}")
			}
	}
}
