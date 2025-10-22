package org.openecard.sc.pace.asn1

import org.openecard.sc.pace.oid.EacObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.toObjectIdentifier
import org.openecard.sc.tlv.toUInt
import org.openecard.sc.tlv.toULong

sealed class AlgorithmIdentifier(
	val algorithm: ObjectIdentifier,
	val parametersRaw: Tlv?,
) {
	class Unknown(
		algorithm: ObjectIdentifier,
		parameters: Tlv?,
	) : AlgorithmIdentifier(algorithm, parameters)

	class StandardizedDomainParametersAlgorithm(
		algorithm: ObjectIdentifier,
		val parameterIndex: UInt,
		parameters: Tlv?,
	) : AlgorithmIdentifier(algorithm, parameters) {
		val standardParameters by lazy {
			StandardizedDomainParameters.forParameterId(parameterIndex)
		}
	}

	class ExplicitDomainParametersAlgorithm(
		algorithm: ObjectIdentifier,
		parameters: Tlv?,
	) : AlgorithmIdentifier(algorithm, parameters)

	companion object {
		@Throws(IllegalArgumentException::class)
		@OptIn(ExperimentalUnsignedTypes::class)
		fun Tlv.toAlgorithmIdentifier(): AlgorithmIdentifier {
			val childTags = requireNotNull(this.asConstructed?.child).asList()
			val algorithm: ObjectIdentifier = childTags[0].toObjectIdentifier()
			val parameters = childTags.getOrNull(1)

			// TODO: add support for explicit domain parameters
			return if (algorithm.value == EacObjectIdentifier.STANDARDIZED_DOMAIN_PARAMETERS) {
				val index =
					requireNotNull(parameters?.toUInt()) { "Standardized domain parameters are missing the parameter argument" }
				StandardizedDomainParametersAlgorithm(algorithm, index, parameters)
			} else {
				Unknown(algorithm, parameters)
			}
		}
	}
}
